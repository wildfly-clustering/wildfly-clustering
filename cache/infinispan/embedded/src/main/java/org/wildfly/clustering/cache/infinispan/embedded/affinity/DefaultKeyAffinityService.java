/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.affinity;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.infinispan.Cache;
import org.infinispan.affinity.KeyAffinityService;
import org.infinispan.affinity.KeyGenerator;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.Listener.Observation;
import org.infinispan.notifications.cachelistener.annotation.TopologyChanged;
import org.infinispan.notifications.cachelistener.event.TopologyChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.infinispan.util.concurrent.BlockingManager;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.KeyDistribution;

/**
 * A custom key affinity service implementation with the following distinct characteristics (as compared to {@link org.infinispan.affinity.impl.KeyAffinityServiceImpl}):
 * <ul>
 * <li>{@link #getKeyForAddress(Address)} will return a random key (instead of throwing an ISE) if the specified address does not own any segments.</li>
 * <li>Uses a worker thread per address for which to generate keys.</li>
 * <li>Minimal CPU utilization when key queues are full.</li>
 * <li>Non-blocking topology change event handler.</li>
 * <li>{@link #getKeyForAddress(Address)} calls will not block during topology change events.</li>
 * </ul>
 * @param <K> the cache key type
 * @author Paul Ferraro
 */
@Listener(observation = Observation.POST)
public class DefaultKeyAffinityService<K> implements KeyAffinityService<K>, Supplier<BlockingQueue<K>> {
	private static final Function<Cache<?, ?>, ConsistentHash> CURRENT_CONSISTENT_HASH = cache -> cache.getAdvancedCache().getDistributionManager().getCacheTopology().getWriteConsistentHash();
	private static final BiFunction<Cache<?, ?>, ConsistentHash, KeyDistribution> KEY_DISTRIBUTION_FACTORY = KeyDistribution::forConsistentHash;

	static final int DEFAULT_QUEUE_SIZE = 100;
	private static final System.Logger LOGGER = System.getLogger(DefaultKeyAffinityService.class.getName());

	private final Cache<? extends K, ?> cache;
	private final KeyGenerator<? extends K> generator;
	private final AtomicReference<KeyAffinityState<K>> currentState = new AtomicReference<>();
	private final Predicate<Address> filter;
	private final Executor executor;
	private final BiFunction<Cache<?, ?>, ConsistentHash, KeyDistribution> distributionFactory;
	private final Function<Cache<?, ?>, ConsistentHash> currentConsistentHash;

	private volatile int queueSize = DEFAULT_QUEUE_SIZE;
	private volatile Duration timeout = Duration.ofMillis(100L);

	private interface KeyAffinityState<K> {
		KeyDistribution getDistribution();
		KeyRegistry<K> getRegistry();
		Iterable<Future<?>> getFutures();
	}

	/**
	 * Constructs a key affinity service that generates keys hashing to the members matching the specified filter.
	 * @param cache the target cache
	 * @param generator a key generator
	 */
	DefaultKeyAffinityService(Cache<? extends K, ?> cache, KeyGenerator<? extends K> generator, Predicate<Address> filter) {
		this(cache, generator, filter, GlobalComponentRegistry.componentOf(cache.getCacheManager(), BlockingManager.class).asExecutor(DefaultKeyAffinityService.class.getSimpleName()), CURRENT_CONSISTENT_HASH, KEY_DISTRIBUTION_FACTORY);
	}

	DefaultKeyAffinityService(Cache<? extends K, ?> cache, KeyGenerator<? extends K> generator, Predicate<Address> filter, Executor executor, Function<Cache<?, ?>, ConsistentHash> currentConsistentHash, BiFunction<Cache<?, ?>, ConsistentHash, KeyDistribution> distributionFactory) {
		this.cache = cache;
		this.generator = generator;
		this.filter = filter;
		this.executor = executor;
		this.currentConsistentHash = currentConsistentHash;
		this.distributionFactory = distributionFactory;
	}

	/**
	 * Overrides the maximum number of keys with affinity to a given member to pre-generate.
	 * @param size a queue size threshold
	 */
	public void setQueueSize(int size) {
		this.queueSize = size;
	}

	/**
	 * Overrides the duration of time for which calls to {@link #getKeyForAddress(Address)} will wait for an available pre-generated key,
	 * after which a random key will be returned.
	 * @param timeout a queue poll timeout
	 */
	public void setPollTimeout(Duration timeout) {
		this.timeout = timeout;
	}

	@Override
	public BlockingQueue<K> get() {
		return new ArrayBlockingQueue<>(this.queueSize);
	}

	@Override
	public boolean isStarted() {
		return this.currentState.get() != null;
	}

	@Override
	public void start() {
		this.accept(this.currentConsistentHash.apply(this.cache));
		this.cache.addListener(this);
	}

	@Override
	public void stop() {
		this.cache.removeListener(this);
		this.currentState.set(null);
	}

	@Override
	public K getCollocatedKey(K otherKey) {
		return this.getCollocatedKey(this.currentState.get(), otherKey);
	}

	private K getCollocatedKey(KeyAffinityState<K> state, K otherKey) {
		if (state != null) {
			K key = this.poll(state.getRegistry(), state.getDistribution().getPrimaryOwner(otherKey));
			if (key != null) {
				return key;
			}
			KeyAffinityState<K> currentState = this.currentState.get();
			// If state is out-dated, retry
			if (state != currentState) {
				return this.getCollocatedKey(currentState, otherKey);
			}
		}
		LOGGER.log(System.Logger.Level.DEBUG, "Could not obtain pre-generated key with same affinity as {0} -- generating random key", otherKey);
		return this.generator.getKey();
	}

	@Override
	public K getKeyForAddress(Address address) {
		if (!this.filter.test(address)) {
			throw new IllegalArgumentException(address.toString());
		}
		return this.getKeyForAddress(this.currentState.get(), address);
	}

	private K getKeyForAddress(KeyAffinityState<K> state, Address address) {
		if (state != null) {
			K key = this.poll(state.getRegistry(), address);
			if (key != null) {
				return key;
			}
			KeyAffinityState<K> currentState = this.currentState.get();
			// If state is out-dated, retry
			if (state != currentState) {
				return this.getKeyForAddress(currentState, address);
			}
		}
		LOGGER.log(System.Logger.Level.DEBUG, "Could not obtain pre-generated key with affinity for {0} -- generating random key", address);
		return this.generator.getKey();
	}

	private K poll(KeyRegistry<K> registry, Address address) {
		BlockingQueue<K> keys = registry.getKeys(address);
		if (keys != null) {
			Duration timeout = this.timeout;
			long nanos = (timeout.getSeconds() == 0) ? timeout.getNano() : timeout.toNanos();
			try {
				return keys.poll(nanos, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return null;
	}

	@TopologyChanged
	public CompletionStage<Void> topologyChanged(TopologyChangedEvent<?, ?> event) {
		if (!this.getSegments(event.getWriteConsistentHashAtStart()).equals(this.getSegments(event.getWriteConsistentHashAtEnd()))) {
			LOGGER.log(System.Logger.Level.DEBUG, "Restarting key generation based on new consistent hash for topology {0}", event.getNewTopologyId());
			this.accept(event.getWriteConsistentHashAtEnd());
		}
		return CompletableFuture.completedStage(null);
	}

	private Map<Address, Set<Integer>> getSegments(ConsistentHash hash) {
		Map<Address, Set<Integer>> segments = new TreeMap<>();
		for (Address address : hash.getMembers()) {
			if (this.filter.test(address)) {
				segments.put(address, hash.getPrimarySegmentsForOwner(address));
			}
		}
		return segments;
	}

	private void accept(ConsistentHash hash) {
		KeyDistribution distribution = this.distributionFactory.apply(this.cache, hash);
		KeyRegistry<K> registry = new ConsistentHashKeyRegistry<>(hash, this.filter, this);
		Set<Address> addresses = registry.getAddresses();
		List<Future<?>> futures = !addresses.isEmpty() ? new ArrayList<>(addresses.size()) : List.of();
		try {
			for (Address address : addresses) {
				BlockingQueue<K> keys = registry.getKeys(address);
				FutureTask<Void> task = new FutureTask<>(new GenerateKeysTask<>(this.generator, distribution, address, keys), null);
				futures.add(task);
				this.executor.execute(task);
			}
			KeyAffinityState<K> previousState = this.currentState.getAndSet(new KeyAffinityState<K>() {
				@Override
				public KeyDistribution getDistribution() {
					return distribution;
				}

				@Override
				public KeyRegistry<K> getRegistry() {
					return registry;
				}

				@Override
				public Iterable<Future<?>> getFutures() {
					return futures;
				}
			});
			if (previousState != null) {
				for (Future<?> future : previousState.getFutures()) {
					future.cancel(true);
				}
			}
		} catch (RejectedExecutionException e) {
			// Executor was shutdown. Cancel any tasks that were already submitted
			for (Future<?> future : futures) {
				future.cancel(true);
			}
		}
	}

	private static class GenerateKeysTask<K> implements Runnable {
		private final KeyGenerator<? extends K> generator;
		private final KeyDistribution distribution;
		private final Address address;
		private final BlockingQueue<K> keys;

		GenerateKeysTask(KeyGenerator<? extends K> generator, KeyDistribution distribution, Address address, BlockingQueue<K> keys) {
			this.generator = generator;
			this.distribution = distribution;
			this.address = address;
			this.keys = keys;
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				K key = this.generator.getKey();
				if (this.distribution.getPrimaryOwner(key).equals(this.address)) {
					try {
						this.keys.put(key);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}
}
