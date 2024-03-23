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
import org.wildfly.clustering.server.provider.ServiceProviderListener;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;
import org.wildfly.clustering.server.provider.ServiceProviderRegistry;

/**
 * Factory that provides a non-clustered {@link ServiceProviderRegistration} implementation.
 * @param <T> the service provider type
 * @author Paul Ferraro
 */
public interface LocalServiceProviderRegistry<T> extends ServiceProviderRegistry<T, LocalGroupMember> {

	@Override
	LocalGroup getGroup();

	static <T> LocalServiceProviderRegistry<T> of(LocalGroup group) {
		Set<T> services = ConcurrentHashMap.newKeySet();
		return new LocalServiceProviderRegistry<>() {

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
			public ServiceProviderRegistration<T, LocalGroupMember> register(T service, ServiceProviderListener<LocalGroupMember> listener) {
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
