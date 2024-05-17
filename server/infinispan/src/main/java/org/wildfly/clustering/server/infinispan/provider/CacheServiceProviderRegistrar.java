/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan.provider;

import java.util.AbstractMap;
import java.util.Collections;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.infinispan.Cache;
import org.infinispan.commons.CacheException;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.context.Flag;
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
import org.jboss.logging.Logger;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.Locality;
import org.wildfly.clustering.context.DefaultExecutorService;
import org.wildfly.clustering.context.ExecutorServiceFactory;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.infinispan.util.CacheInvoker;
import org.wildfly.clustering.server.local.provider.DefaultServiceProviderRegistration;
import org.wildfly.clustering.server.provider.ServiceProviderListener;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrar;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;
import org.wildfly.clustering.server.util.Invoker;
import org.wildfly.common.function.ExceptionRunnable;

/**
 * Infinispan {@link Cache} based {@link ServiceProviderRegistrar}.
 * This factory can create multiple {@link ServiceProviderRegistration} instances, all of which share the same {@link Cache} instance.
 * @author Paul Ferraro
 * @param <T> the service identifier type
 */
@Listener(observation = Observation.POST)
public class CacheServiceProviderRegistrar<T> implements CacheContainerServiceProviderRegistrar<T>, AutoCloseable {
	private static final Logger LOGGER = Logger.getLogger(CacheServiceProviderRegistrar.class);

	private final Supplier<Batch> batchFactory;
	private final ConcurrentMap<T, Map.Entry<ServiceProviderListener<CacheContainerGroupMember>, ExecutorService>> listeners = new ConcurrentHashMap<>();
	private final Cache<T, Set<Address>> cache;
	private final CacheContainerGroup group;
	private final Invoker invoker;
	private final Executor executor;

	public CacheServiceProviderRegistrar(CacheServiceProviderRegistrarConfiguration config) {
		this.group = config.getGroup();
		this.cache = config.getCache();
		this.batchFactory = config.getBatchFactory();
		this.executor = config.getExecutor();
		this.invoker = CacheInvoker.retrying(this.cache);
		this.cache.addListener(this);
	}

	@Override
	public void close() {
		this.cache.removeListener(this);
		// Cleanup any unclosed registrations
		for (Map.Entry<ServiceProviderListener<CacheContainerGroupMember>, ExecutorService> entry : this.listeners.values()) {
			ExecutorService executor = entry.getValue();
			if (executor != null) {
				this.shutdown(executor);
			}
		}
		this.listeners.clear();
	}

	private void shutdown(ExecutorService executor) {
		executor.shutdown();
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
		return this.register(service, null);
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
		this.invoker.invoke(new RegisterLocalServiceTask(service));
		Address localAddress = this.cache.getCacheManager().getAddress();
		return new DefaultServiceProviderRegistration<>(this, service, () -> {
			try (Batch batch = this.batchFactory.get()) {
				this.cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).compute(service, new AddressSetRemoveFunction(localAddress));
			} finally {
				Map.Entry<ServiceProviderListener<CacheContainerGroupMember>, ExecutorService> oldEntry = this.listeners.remove(service);
				if (oldEntry != null) {
					ExecutorService executor = oldEntry.getValue();
					if (executor != null) {
						this.shutdown(executor);
					}
				}
			}
		});
	}

	void registerLocal(T service) {
		Address localAddress = this.cache.getCacheManager().getAddress();
		try (Batch batch = this.batchFactory.get()) {
			this.register(localAddress, service);
		}
	}

	void register(Address address, T service) {
		this.cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).compute(service, new AddressSetAddFunction(address));
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

			// If this is a merge after cluster split: Re-assert services for local member
			Set<T> localServices = !previousMembers.contains(localAddress) ? this.listeners.keySet() : Collections.emptySet();

			if (!leftMembers.isEmpty() || !localServices.isEmpty()) {
				try {
					this.executor.execute(() -> {
						if (!leftMembers.isEmpty()) {
							try (Batch batch = this.batchFactory.get()) {
								try (CloseableIterator<T> keys = cache.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).keySet().iterator()) {
									while (keys.hasNext()) {
										cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).compute(keys.next(), new AddressSetRemoveFunction(leftMembers));
									}
								}
							}
						}
						if (!localServices.isEmpty()) {
							for (T localService : localServices) {
								this.invoker.invoke(new RegisterLocalServiceTask(localService));
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
							LOGGER.warn(e.getLocalizedMessage(), e);
						}
					});
				} catch (RejectedExecutionException e) {
					// Executor was shutdown
				}
			}
		}
		return CompletableFuture.completedStage(null);
	}

	private class RegisterLocalServiceTask implements ExceptionRunnable<CacheException> {
		private final T localService;

		RegisterLocalServiceTask(T localService) {
			this.localService = localService;
		}

		@Override
		public void run() {
			CacheServiceProviderRegistrar.this.registerLocal(this.localService);
		}
	}
}
