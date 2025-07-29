/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan.provider;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.infinispan.Cache;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.Listener.Observation;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.TopologyChanged;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.TopologyChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.CacheStreamFilter;
import org.wildfly.clustering.context.DefaultExecutorService;
import org.wildfly.clustering.context.ExecutorServiceFactory;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.local.provider.DefaultServiceProviderRegistration;
import org.wildfly.clustering.server.provider.ServiceProviderListener;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrar;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;

/**
 * Infinispan {@link Cache} based {@link ServiceProviderRegistrar}.
 * This factory can create multiple {@link ServiceProviderRegistration} instances, all of which share the same {@link Cache} instance.
 * @author Paul Ferraro
 * @param <T> the service identifier type
 */
@Listener(observation = Observation.POST)
public class CacheServiceProviderRegistrar<T> implements CacheContainerServiceProviderRegistrar<T>, AutoCloseable {
	private static final System.Logger LOGGER = System.getLogger(CacheServiceProviderRegistrar.class.getName());

	private final Supplier<Batch> batchFactory;
	private final ConcurrentMap<T, Map.Entry<ServiceProviderListener<CacheContainerGroupMember>, ExecutorService>> listeners = new ConcurrentHashMap<>();
	private final Cache<T, Set<Address>> cache;
	private final CacheContainerGroup group;
	private final Executor executor;

	public CacheServiceProviderRegistrar(CacheServiceProviderRegistrarConfiguration config) {
		this.group = config.getGroup();
		this.cache = config.getWriteOnlyCache();
		this.batchFactory = config.getBatchFactory();
		this.executor = config.getExecutor();
		this.cache.addListener(this);
	}

	@Override
	public void close() {
		this.cache.removeListener(this);
		// Cleanup any unclosed registrations
		for (T service : this.listeners.keySet()) {
			this.unregisterLocal(service);
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

	@Override
	public CacheContainerGroup getGroup() {
		return this.group;
	}

	@Override
	public ServiceProviderRegistration<T, CacheContainerGroupMember> register(T service) {
		return this.register(service, (ServiceProviderListener<CacheContainerGroupMember>) null);
	}

	@Override
	public ServiceProviderRegistration<T, CacheContainerGroupMember> register(T service, ServiceProviderListener<CacheContainerGroupMember> listener) {
		Map.Entry<ServiceProviderListener<CacheContainerGroupMember>, ExecutorService> newEntry = new AbstractMap.SimpleEntry<>(listener, null);
		// Only create executor for new registrations
		Map.Entry<ServiceProviderListener<CacheContainerGroupMember>, ExecutorService> entry = this.listeners.computeIfAbsent(service, key -> {
			if (listener != null) {
				newEntry.setValue(new DefaultExecutorService(ExecutorServiceFactory.SINGLE_THREAD, Thread.currentThread().getContextClassLoader()));
			}
			return newEntry;
		});
		if (entry != newEntry) {
			throw new IllegalArgumentException(service.toString());
		}
		this.registerLocal(service);
		return new DefaultServiceProviderRegistration<>(this, service, () -> this.unregisterLocal(service));
	}

	void registerLocal(T service) {
		Address localAddress = this.cache.getCacheManager().getAddress();
		try (Batch batch = this.batchFactory.get()) {
			this.register(service, localAddress);
		}
	}

	void register(T service, Address address) {
		this.cache.compute(service, new AddressSetAddFunction(address));
	}

	void unregisterLocal(T service) {
		try {
			Address localAddress = this.cache.getCacheManager().getAddress();
			try (Batch batch = this.batchFactory.get()) {
				this.unregister(service, Set.of(localAddress));
			}
		} finally {
			Map.Entry<ServiceProviderListener<CacheContainerGroupMember>, ExecutorService> oldEntry = this.listeners.remove(service);
			if (oldEntry != null) {
				ExecutorService executor = oldEntry.getValue();
				if (executor != null) {
					this.shutdown(executor);
				}
			}
		}
	}

	void unregister(T service, Set<Address> addresses) {
		this.cache.compute(service, new AddressSetRemoveFunction(addresses));
	}

	@Override
	public Set<CacheContainerGroupMember> getProviders(final T service) {
		Set<Address> addresses = this.cache.get(service);
		return (addresses != null) ? this.map(addresses) : Set.of();
	}

	@Override
	public Set<T> getServices() {
		return this.cache.keySet();
	}

	private Set<CacheContainerGroupMember> map(Set<Address> addresses) {
		return addresses.stream().map(this.group.getGroupMemberFactory()::createGroupMember).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
	}

	@TopologyChanged
	public CompletionStage<Void> topologyChanged(TopologyChangedEvent<T, Set<Address>> event) {
		// A singleton group does not care about topology changes
		if (this.group.isSingleton()) return CompletableFuture.completedStage(null);

		ConsistentHash previousHash = event.getWriteConsistentHashAtStart();
		List<Address> previousMembers = previousHash.getMembers();
		ConsistentHash hash = event.getWriteConsistentHashAtEnd();
		List<Address> members = hash.getMembers();

		if (!members.equals(previousMembers)) {
			Cache<T, Set<Address>> cache = event.getCache();
			Address localAddress = cache.getCacheManager().getAddress();

			// Determine which group members have left the cache view
			Set<Address> leftMembers = new HashSet<>(previousMembers);
			leftMembers.removeAll(members);

			// If this is a merge after cluster split: Re-assert services for local member
			Set<T> localServices = !previousMembers.contains(localAddress) ? this.listeners.keySet() : Set.of();

			if (!leftMembers.isEmpty() || !localServices.isEmpty()) {
				try {
					this.executor.execute(() -> {
						if (!leftMembers.isEmpty()) {
							try (Batch batch = this.batchFactory.get()) {
								// Filter keys owned by the local member
								CacheStreamFilter<T> filter = CacheStreamFilter.primary(hash, localAddress);
								try (Stream<T> stream = filter.apply(cache.keySet().stream())) {
									Iterator<T> keys = stream.iterator();
									while (keys.hasNext()) {
										this.unregister(keys.next(), leftMembers);
									}
								}
							}
						}
						if (!localServices.isEmpty()) {
							for (T localService : localServices) {
								this.registerLocal(localService);
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

	@CacheEntryCreated
	public CompletionStage<Void> created(CacheEntryCreatedEvent<T, Set<Address>> event) {
		return this.updated(event.getKey(), event.getValue());
	}

	@CacheEntryModified
	public CompletionStage<Void> modified(CacheEntryModifiedEvent<T, Set<Address>> event) {
		return !Objects.equals(event.getOldValue(), event.getNewValue()) ? this.updated(event.getKey(), event.getNewValue()) : CompletableFuture.completedFuture(null);
	}

	private CompletionStage<Void> updated(T service, Set<Address> providers) {
		Map.Entry<ServiceProviderListener<CacheContainerGroupMember>, ExecutorService> entry = this.listeners.get(service);
		if (entry != null) {
			ServiceProviderListener<CacheContainerGroupMember> listener = entry.getKey();
			if (listener != null) {
				Executor executor = entry.getValue();
				Set<CacheContainerGroupMember> members = this.map(providers);
				try {
					executor.execute(() -> {
						try {
							listener.providersChanged(members);
						} catch (Throwable e) {
							LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
						}
					});
				} catch (RejectedExecutionException e) {
					// Executor was shutdown
				}
			}
		}
		return CompletableFuture.completedStage(null);
	}
}
