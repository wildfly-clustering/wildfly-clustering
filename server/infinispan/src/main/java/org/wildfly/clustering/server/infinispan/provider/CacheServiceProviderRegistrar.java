/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan.provider;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.infinispan.Cache;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.Listener.Observation;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.annotation.TopologyChanged;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachelistener.event.TopologyChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.CacheStreamFilter;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.listener.ListenerRegistry;
import org.wildfly.clustering.server.local.listener.LocalListenerRegistry;
import org.wildfly.clustering.server.local.provider.DefaultServiceProviderRegistration;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrar;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrationEvent;
import org.wildfly.clustering.server.provider.ServiceProviderRegistryListener;

/**
 * Infinispan {@link Cache} based {@link ServiceProviderRegistrar}.
 * This factory can create multiple {@link ServiceProviderRegistration} instances, all of which share the same {@link Cache} instance.
 * @author Paul Ferraro
 * @param <T> the service identifier type
 */
@Listener(observation = Observation.POST)
public class CacheServiceProviderRegistrar<T> implements CacheContainerServiceProviderRegistrar<T>, AutoCloseable {

	/**
	 * The configuration of this service provider registrar.
	 */
	public interface Configuration extends EmbeddedCacheConfiguration {
		/**
		 * Returns the group with which this service provider registrar is associated.
		 * @return the group with which this service provider registrar is associated.
		 */
		CacheContainerGroup getGroup();
	}

	private final Supplier<Batch> batchFactory;
	private final Set<T> localServices = ConcurrentHashMap.newKeySet();
	private final ListenerRegistry<ServiceProviderRegistryListener<T, CacheContainerGroupMember>> listeners;
	private final Cache<T, Set<Address>> cache;
	private final BooleanSupplier active;
	private final CacheContainerGroup group;
	private final Executor executor;

	/**
	 * Creates a service provider registrar using the specified configuration
	 * @param configuration a service provider registrar configuration
	 */
	public CacheServiceProviderRegistrar(Configuration configuration) {
		this.group = configuration.getGroup();
		this.cache = configuration.getWriteOnlyCache();
		this.batchFactory = configuration.getBatchFactory();
		this.executor = configuration.getExecutor();
		this.listeners = new LocalListenerRegistry<>(configuration.getStopTimeout());
		this.active = configuration::isActive;
		this.cache.addListener(this);
	}

	@Override
	public void close() {
		this.cache.removeListener(this);
		this.listeners.close();
	}

	@Override
	public CacheContainerGroup getGroup() {
		return this.group;
	}

	@Override
	public ServiceProviderRegistration<T, CacheContainerGroupMember> register(T service) {
		if (!this.localServices.add(service)) {
			throw new IllegalArgumentException(service.toString());
		}
		if (this.active.getAsBoolean()) {
			this.registerLocalMember(service);
		}
		return new DefaultServiceProviderRegistration<>(this, service, () -> {
			try {
				this.unregisterLocal(service);
			} finally {
				this.localServices.remove(service);
			}
		});
	}

	@Override
	public Registration register(ServiceProviderRegistryListener<T, CacheContainerGroupMember> listener) {
		return this.listeners.register(listener);
	}

	void registerLocalMember(T service) {
		Address localAddress = this.cache.getCacheManager().getAddress();
		try (Batch batch = this.batchFactory.get()) {
			this.registerMember(service, localAddress);
		}
	}

	void registerMember(T service, Address address) {
		this.cache.compute(service, new AddressSetAddFunction(address));
	}

	void unregisterLocal(T service) {
		Address localAddress = this.cache.getCacheManager().getAddress();
		try (Batch batch = this.batchFactory.get()) {
			this.unregister(service, Set.of(localAddress));
		}
	}

	void unregister(T service, Set<Address> addresses) {
		this.cache.compute(service, new AddressSetRemoveFunction(addresses));
	}

	@Override
	public Set<CacheContainerGroupMember> getProviders(T service) {
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

	/**
	 * Non-blocking handler of topology changed events.
	 * @param event a topology changed event
	 * @return a completion stage
	 */
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
			Set<T> localServices = !previousMembers.contains(localAddress) ? this.localServices : Set.of();

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
						if (!localServices.isEmpty() && this.active.getAsBoolean()) {
							for (T localService : localServices) {
								this.registerLocalMember(localService);
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
	 * Non-blocking handler of cache entry creation events.
	 * @param event a cache entry creation event
	 * @return a completion stage
	 */
	@CacheEntryCreated
	public CompletionStage<Void> created(CacheEntryCreatedEvent<T, Set<Address>> event) {
		return this.updated(event.getKey(), Set.of(), event.getValue());
	}

	/**
	 * Non-blocking handler of cache entry modified events.
	 * @param event a cache entry modified event
	 * @return a completion stage
	 */
	@CacheEntryModified
	public CompletionStage<Void> modified(CacheEntryModifiedEvent<T, Set<Address>> event) {
		return !Objects.equals(event.getOldValue(), event.getNewValue()) ? this.updated(event.getKey(), event.getOldValue(), event.getNewValue()) : CompletableFuture.completedFuture(null);
	}

	/**
	 * Non-blocking handler of cache entry removed events.
	 * @param event a cache entry removed event
	 * @return a completion stage
	 */
	@CacheEntryRemoved
	public CompletionStage<Void> removed(CacheEntryRemovedEvent<T, Set<Address>> event) {
		return this.updated(event.getKey(), event.getOldValue(), Set.of());
	}

	private CompletionStage<Void> updated(T service, Set<Address> previousProviders, Set<Address> currentProviders) {
		Set<CacheContainerGroupMember> previousMembers = this.map(previousProviders);
		Set<CacheContainerGroupMember> currentMembers = this.map(currentProviders);
		ServiceProviderRegistrationEvent<CacheContainerGroupMember> event = new ServiceProviderRegistrationEvent<>() {
			@Override
			public Set<CacheContainerGroupMember> getPreviousProviders() {
				return previousMembers;
			}

			@Override
			public Set<CacheContainerGroupMember> getCurrentProviders() {
				return currentMembers;
			}
		};
		this.listeners.accept(listener -> listener.providersChanged(service, event));
		return CompletableFuture.completedStage(null);
	}
}
