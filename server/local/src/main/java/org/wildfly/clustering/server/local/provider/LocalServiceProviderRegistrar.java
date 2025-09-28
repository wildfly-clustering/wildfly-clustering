/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.local.provider;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.wildfly.clustering.server.local.LocalGroup;
import org.wildfly.clustering.server.local.LocalGroupMember;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrar;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrationListener;

/**
 * Factory that provides a non-clustered {@link ServiceProviderRegistration} implementation.
 * @param <T> the service provider type
 * @author Paul Ferraro
 */
public interface LocalServiceProviderRegistrar<T> extends ServiceProviderRegistrar<T, LocalGroupMember> {

	@Override
	LocalGroup getGroup();

	/**
	 * Creates a local service provider registrar for the specified group.
	 * @param <T> the service type
	 * @param group a local group
	 * @return a local service provider registrar for the specified group.
	 */
	static <T> LocalServiceProviderRegistrar<T> of(LocalGroup group) {
		Set<T> services = ConcurrentHashMap.newKeySet();
		return new LocalServiceProviderRegistrar<>() {
			@Override
			public LocalGroup getGroup() {
				return group;
			}

			@Override
			public ServiceProviderRegistration<T, LocalGroupMember> register(T service) {
				services.add(service);
				return new DefaultServiceProviderRegistration<>(this, service, () -> services.remove(service));
			}

			@Override
			public ServiceProviderRegistration<T, LocalGroupMember> register(T service, ServiceProviderRegistrationListener<LocalGroupMember> listener) {
				return this.register(service);
			}

			@Override
			public Set<LocalGroupMember> getProviders(T service) {
				return services.contains(service) ? Set.of(group.getLocalMember()) : Set.of();
			}

			@Override
			public Set<T> getServices() {
				return Collections.unmodifiableSet(services);
			}
		};
	}
}
