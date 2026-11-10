/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.provider;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.listener.ListenerRegistry;
import org.wildfly.clustering.server.local.LocalGroup;
import org.wildfly.clustering.server.local.LocalGroupMember;
import org.wildfly.clustering.server.local.listener.LocalListenerRegistry;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrationEvent;
import org.wildfly.clustering.server.provider.ServiceProviderRegistryListener;

/**
 * Local service provider registrar.
 * @author Paul Ferraro
 * @param <T> the service type
 */
public class DefaultLocalServiceProviderRegistrar<T> implements LocalServiceProviderRegistrar<T> {
	private final LocalGroup group;
	private final Set<T> services = ConcurrentHashMap.newKeySet();
	private final ListenerRegistry<ServiceProviderRegistryListener<T, LocalGroupMember>> listeners = new LocalListenerRegistry<>(Duration.ZERO);

	/**
	 * Constructs a service provider registrar for the specified local group.
	 * @param group a local group
	 */
	public DefaultLocalServiceProviderRegistrar(LocalGroup group) {
		this.group = group;
	}

	@Override
	public void close() {
		this.listeners.close();
	}

	@Override
	public LocalGroup getGroup() {
		return this.group;
	}

	@Override
	public Set<LocalGroupMember> getProviders(T service) {
		return this.services.contains(service) ? Set.of(this.group.getLocalMember()) : Set.of();
	}

	@Override
	public Set<T> getServices() {
		return Collections.unmodifiableSet(this.services);
	}

	@Override
	public ServiceProviderRegistration<T, LocalGroupMember> register(T service) {
		if (!this.services.add(service)) {
			throw new IllegalArgumentException(service.toString());
		}
		Set<LocalGroupMember> singleton = Set.of(this.group.getLocalMember());
		ServiceProviderRegistrationEvent<LocalGroupMember> addEvent = createEvent(Set.of(), singleton);
		this.listeners.accept(listener -> listener.providersChanged(service, addEvent));
		return new DefaultServiceProviderRegistration<>(this, service, () -> {
			this.services.remove(service);
			ServiceProviderRegistrationEvent<LocalGroupMember> removeEvent = createEvent(singleton, Set.of());
			this.listeners.accept(listener -> listener.providersChanged(service, removeEvent));
		});
	}

	@Override
	public Registration register(ServiceProviderRegistryListener<T, LocalGroupMember> listener) {
		return this.listeners.register(listener);
	}

	private static ServiceProviderRegistrationEvent<LocalGroupMember> createEvent(Set<LocalGroupMember> previousProviders, Set<LocalGroupMember> currentProviders) {
		return new ServiceProviderRegistrationEvent<>() {
			@Override
			public Set<LocalGroupMember> getPreviousProviders() {
				return previousProviders;
			}

			@Override
			public Set<LocalGroupMember> getCurrentProviders() {
				return currentProviders;
			}
		};
	}
}
