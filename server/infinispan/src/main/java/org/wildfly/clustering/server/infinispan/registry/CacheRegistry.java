/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan.registry;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.infinispan.Cache;
import org.infinispan.commons.CacheException;
import org.infinispan.context.Flag;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.Listener.Observation;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.annotation.TopologyChanged;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachelistener.event.Event;
import org.infinispan.notifications.cachelistener.event.TopologyChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.Locality;
import org.wildfly.clustering.cache.infinispan.embedded.listener.KeyFilter;
import org.wildfly.clustering.context.DefaultExecutorService;
import org.wildfly.clustering.function.Runner;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.registry.Registry;
import org.wildfly.clustering.server.registry.RegistryListener;
import org.wildfly.clustering.server.util.MapEntry;

/**
 * Clustered {@link Registry} backed by an Infinispan cache.
 * @author Paul Ferraro
 * @param <K> key type
 * @param <V> value type
 */
@Listener(observation = Observation.POST)
public class CacheRegistry<K, V> implements CacheContainerRegistry<K, V> {
	private static final System.Logger LOGGER = System.getLogger(CacheRegistry.class.getName());

	/**
	 * The configuration of this registry.
	 * @param <K> the registry key type
	 * @param <V> the registry value type
	 */
	public interface Configuration<K, V> extends EmbeddedCacheConfiguration {
		/**
		 * Returns the group with which this registry is associated.
		 * @return the group with which this registry is associated.
		 */
		CacheContainerGroup getGroup();

		/**
		 * Returns the entry of the local member.
		 * @return the entry of the local member.
		 */
		Map.Entry<K, V> getEntry();

		/**
		 * Returns a task to run on {@link CacheContainerRegistry#close()}.
		 * @return a task to run on {@link CacheContainerRegistry#close()}.
		 */
		default Runnable getCloseTask() {
			return Runner.empty();
		}
	}

	private final Map<RegistryListener<K, V>, ExecutorService> listeners = new ConcurrentHashMap<>();
	private final Cache<Address, Map.Entry<K, V>> cache;
	private final Supplier<Batch> batchFactory;
	private final CacheContainerGroup group;
	private final Runnable closeTask;
	private final Map.Entry<K, V> entry;
	private final Executor executor;
	private final BooleanSupplier active;
	private final Function<RegistryListener<K, V>, ExecutorService> executorServiceFactory = new Function<>() {
		@Override
		public ExecutorService apply(RegistryListener<K, V> listener) {
			return new DefaultExecutorService(Executors::newSingleThreadExecutor, Thread.currentThread().getContextClassLoader());
		}
	};

	/**
	 * Creates a cache registry using the specified configuration.
	 * @param configuration the configuration of this registry.
	 */
	public CacheRegistry(Configuration<K, V> configuration) {
		this.cache = configuration.getWriteOnlyCache();
		this.batchFactory = configuration.getBatchFactory();
		this.group = configuration.getGroup();
		this.closeTask = configuration.getCloseTask();
		this.executor = configuration.getExecutor();
		this.entry = MapEntry.of(configuration.getEntry().getKey(), configuration.getEntry().getValue());
		this.active = configuration::isActive;
		if (this.active.getAsBoolean()) {
			Address localAddress = this.cache.getCacheManager().getAddress();
			try (Batch batch = this.batchFactory.get()) {
				this.cache.put(localAddress, this.entry);
			}
		}
		if (!this.group.isSingleton()) {
			this.cache.addListener(this, new KeyFilter<>(Address.class), null);
		}
	}

	@Override
	public void close() {
		if (!this.group.isSingleton()) {
			this.cache.removeListener(this);
		}
		try {
			if (this.active.getAsBoolean()) {
				Address localAddress = this.cache.getCacheManager().getAddress();
				try (Batch batch = this.batchFactory.get()) {
					// If this remove fails, the entry will be auto-removed on topology change by the new primary owner
					this.cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES, Flag.FAIL_SILENTLY).remove(localAddress);
				} catch (CacheException e) {
					LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
				}
			}
		} finally {
			// Cleanup any unregistered listeners
			for (ExecutorService executor : this.listeners.values()) {
				this.shutdown(executor);
			}
			this.listeners.clear();
			this.closeTask.run();
		}
	}

	@Override
	public Registration register(RegistryListener<K, V> listener) {
		if (this.group.isSingleton()) {
			return Registration.EMPTY;
		}
		this.listeners.computeIfAbsent(listener, this.executorServiceFactory);
		return () -> this.unregister(listener);
	}

	private void unregister(RegistryListener<K, V> listener) {
		ExecutorService executor = this.listeners.remove(listener);
		if (executor != null) {
			this.shutdown(executor);
		}
	}

	@Override
	public CacheContainerGroup getGroup() {
		return this.group;
	}

	@Override
	public Map<K, V> getEntries() {
		Set<Address> addresses = this.group.getMembership().getMembers().stream().map(CacheContainerGroupMember::getId).collect(Collectors.toUnmodifiableSet());
		Map<K, V> result = new HashMap<>();
		for (Map.Entry<K, V> entry : this.cache.getAdvancedCache().getAll(addresses).values()) {
			result.put(entry.getKey(), entry.getValue());
		}
		return Collections.unmodifiableMap(result);
	}

	@Override
	public Map.Entry<K, V> getEntry(CacheContainerGroupMember member) {
		return this.cache.get(member.getId());
	}

	/**
	 * Non-blocking handler of topology changed events.
	 * @param event a topology changed event.
	 * @return a completion stage
	 */
	@TopologyChanged
	public CompletionStage<Void> topologyChanged(TopologyChangedEvent<Address, Map.Entry<K, V>> event) {
		ConsistentHash previousHash = event.getWriteConsistentHashAtStart();
		List<Address> previousMembers = previousHash.getMembers();
		ConsistentHash hash = event.getWriteConsistentHashAtEnd();
		List<Address> members = hash.getMembers();

		if (!members.equals(previousMembers)) {
			Cache<Address, Map.Entry<K, V>> cache = event.getCache();
			EmbeddedCacheManager container = cache.getCacheManager();
			Address localAddress = container.getAddress();

			// Determine which group members have left the cache view
			Set<Address> leftMembers = new HashSet<>(previousMembers);
			leftMembers.removeAll(members);

			if (!leftMembers.isEmpty()) {
				Locality locality = Locality.forConsistentHash(cache, hash);
				// We're only interested in the entries for which we are the primary owner
				Iterator<Address> addresses = leftMembers.iterator();
				while (addresses.hasNext()) {
					if (!locality.isLocal(addresses.next())) {
						addresses.remove();
					}
				}
			}

			// If this is a merge after cluster split, re-populate the cache registry with lost registry entries
			boolean restoreLocalEntry = !previousMembers.contains(localAddress) && this.active.getAsBoolean();

			if (!leftMembers.isEmpty() || restoreLocalEntry) {
				try {
					this.executor.execute(() -> {
						if (!leftMembers.isEmpty()) {
							Map<K, V> removed = new HashMap<>();
							try {
								for (Address leftMember: leftMembers) {
									Map.Entry<K, V> old = cache.remove(leftMember);
									if (old != null) {
										removed.put(old.getKey(), old.getValue());
									}
								}
							} catch (CacheException e) {
								LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
							}
							if (!removed.isEmpty()) {
								this.notifyListeners(Event.Type.CACHE_ENTRY_REMOVED, removed);
							}
						}
						if (restoreLocalEntry) {
							// If we were not a member at merge start, its mapping may have been lost and need to be recreated
							try {
								if (cache.put(localAddress, this.entry) == null) {
									// Local cache events do not trigger notifications
									this.notifyListeners(Event.Type.CACHE_ENTRY_CREATED, this.entry);
								}
							} catch (CacheException e) {
								LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
							}
						}
					});
				} catch (RejectedExecutionException e) {
					// Executor was shutdown
				}
			}
		}
		return CompletableFuture.completedStage(null);
	}

	/**
	 * Non-blocking handler of cache entry creation/modified events.
	 * @param event a cache entry event.
	 * @return a completion stage
	 */
	@CacheEntryCreated
	@CacheEntryModified
	public CompletionStage<Void> event(CacheEntryEvent<Address, Map.Entry<K, V>> event) {
		if (!event.isOriginLocal()) {
			Map.Entry<K, V> entry = event.getValue();
			if (entry != null) {
				this.executor.execute(() -> this.notifyListeners(event.getType(), entry));
			}
		}
		return CompletableFuture.completedStage(null);
	}

	/**
	 * Non-blocking handler of cache entry removal events.
	 * @param event a cache entry removal event.
	 * @return a completion stage
	 */
	@CacheEntryRemoved
	public CompletionStage<Void> removed(CacheEntryRemovedEvent<Address, Map.Entry<K, V>> event) {
		if (!event.isOriginLocal()) {
			Map.Entry<K, V> entry = event.getOldValue();
			// WFLY-4938 For some reason, the old value can be null
			if (entry != null) {
				this.executor.execute(() -> this.notifyListeners(event.getType(), entry));
			}
		}
		return CompletableFuture.completedStage(null);
	}

	private void notifyListeners(Event.Type type, Map.Entry<K, V> entry) {
		this.notifyListeners(type, Collections.singletonMap(entry.getKey(), entry.getValue()));
	}

	private void notifyListeners(Event.Type type, Map<K, V> entries) {
		for (Map.Entry<RegistryListener<K, V>, ExecutorService> entry: this.listeners.entrySet()) {
			RegistryListener<K, V> listener = entry.getKey();
			Executor executor = entry.getValue();
			try {
				executor.execute(() -> {
					try {
						switch (type) {
							case CACHE_ENTRY_CREATED -> listener.added(entries);
							case CACHE_ENTRY_MODIFIED -> listener.updated(entries);
							case CACHE_ENTRY_REMOVED -> listener.removed(entries);
							default -> throw new IllegalStateException(type.name());
						}
					} catch (Throwable e) {
						LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
					}
				});
			} catch (RejectedExecutionException e) {
				// Executor was shutdown
			}
		}
	}

	@SuppressWarnings("removal")
	private void shutdown(ExecutorService executor) {
		PrivilegedAction<Void> action = new PrivilegedAction<>() {
			@Override
			public Void run() {
				executor.shutdown();
				return null;
			}
		};
		AccessController.doPrivileged(action);
		try {
			executor.awaitTermination(this.cache.getCacheConfiguration().transaction().cacheStopTimeout(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
