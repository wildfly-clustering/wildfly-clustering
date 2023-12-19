/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan.registry;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
import org.jboss.logging.Logger;
import org.wildfly.clustering.cache.batch.Batcher;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.Locality;
import org.wildfly.clustering.cache.infinispan.embedded.listener.KeyFilter;
import org.wildfly.clustering.context.DefaultExecutorService;
import org.wildfly.clustering.context.ExecutorServiceFactory;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.infinispan.util.CacheInvoker;
import org.wildfly.clustering.server.registry.Registry;
import org.wildfly.clustering.server.registry.RegistryListener;
import org.wildfly.common.function.ExceptionRunnable;

/**
 * Clustered {@link Registry} backed by an Infinispan cache.
 * @author Paul Ferraro
 * @param <K> key type
 * @param <V> value type
 */
@Listener(observation = Observation.POST)
public class CacheRegistry<K, V> implements Registry<CacheContainerGroupMember, K, V>, ExceptionRunnable<CacheException> {
	private static final Logger LOGGER = Logger.getLogger(CacheRegistry.class);

	private final Map<RegistryListener<K, V>, ExecutorService> listeners = new ConcurrentHashMap<>();
	private final Cache<Address, Map.Entry<K, V>> cache;
	private final Batcher<TransactionBatch> batcher;
	private final CacheContainerGroup group;
	private final Runnable closeTask;
	private final Map.Entry<K, V> entry;
	private final Executor executor;
	private final Function<RegistryListener<K, V>, ExecutorService> executorServiceFactory = listener -> new DefaultExecutorService(listener.getClass(), ExecutorServiceFactory.SINGLE_THREAD);

	public CacheRegistry(CacheRegistryConfiguration config, Map.Entry<K, V> entry, Runnable closeTask) {
		this.cache = config.getCache();
		this.batcher = config.getBatcher();
		this.group = config.getGroup();
		this.closeTask = closeTask;
		this.executor = config.getBlockingManager().asExecutor(this.getClass().getName());
		this.entry = new AbstractMap.SimpleImmutableEntry<>(entry);
		CacheInvoker.retrying(this.cache).invoke(this);
		this.cache.addListener(this, new KeyFilter<>(Address.class), null);
	}

	@Override
	public void run() {
		Address localAddress = this.cache.getCacheManager().getAddress();
		try (TransactionBatch batch = this.batcher.createBatch()) {
			this.cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(localAddress, this.entry);
		}
	}

	@Override
	public void close() {
		this.cache.removeListener(this);
		Address localAddress = this.cache.getCacheManager().getAddress();
		try (TransactionBatch batch = this.batcher.createBatch()) {
			// If this remove fails, the entry will be auto-removed on topology change by the new primary owner
			this.cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES, Flag.FAIL_SILENTLY).remove(localAddress);
		} catch (CacheException e) {
			LOGGER.warn(e.getLocalizedMessage(), e);
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
		Set<Address> addresses = this.group.getMembership().getMembers().stream().map(CacheContainerGroupMember::getAddress).collect(Collectors.toUnmodifiableSet());
		return this.cache.getAdvancedCache().getAll(addresses).values().stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public Map.Entry<K, V> getEntry(CacheContainerGroupMember member) {
		return this.cache.get(member.getAddress());
	}

	@TopologyChanged
	public CompletionStage<Void> topologyChanged(TopologyChangedEvent<Address, Map.Entry<K, V>> event) {
		ConsistentHash previousHash = event.getWriteConsistentHashAtStart();
		List<Address> previousMembers = previousHash.getMembers();
		ConsistentHash hash = event.getWriteConsistentHashAtEnd();
		List<Address> members = hash.getMembers();

		if (!members.equals(previousMembers)) {
			Cache<Address, Map.Entry<K, V>> cache = event.getCache().getAdvancedCache().withFlags(Flag.FORCE_SYNCHRONOUS);
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

			// If this is a merge after cluster split: re-populate the cache registry with lost registry entries
			boolean restoreLocalEntry = !previousMembers.contains(localAddress);

			if (!leftMembers.isEmpty() || restoreLocalEntry) {
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
							LOGGER.warn(e.getLocalizedMessage(), e);
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
							LOGGER.warn(e.getLocalizedMessage(), e);
						}
					}
				});
			}
		}
		return CompletableFuture.completedStage(null);
	}

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
							case CACHE_ENTRY_CREATED: {
								listener.addedEntries(entries);
								break;
							}
							case CACHE_ENTRY_MODIFIED: {
								listener.updatedEntries(entries);
								break;
							}
							case CACHE_ENTRY_REMOVED: {
								listener.removedEntries(entries);
								break;
							}
							default: {
								throw new IllegalStateException(type.name());
							}
						}
					} catch (Throwable e) {
						LOGGER.warn(e.getLocalizedMessage(), e);
					}
				});
			} catch (RejectedExecutionException e) {
				// Executor was shutdown
			}
		}
	}

	private void shutdown(ExecutorService executor) {
		java.security.AccessController.doPrivileged(DefaultExecutorService.shutdown(executor));
		try {
			executor.awaitTermination(this.cache.getCacheConfiguration().transaction().cacheStopTimeout(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
