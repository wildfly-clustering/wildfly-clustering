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
public class LocalServiceProviderRegistry<T> implements ServiceProviderRegistry<T, LocalGroupMember> {

	private final Set<T> services = ConcurrentHashMap.newKeySet();
	private final LocalGroup group;

	public LocalServiceProviderRegistry(LocalGroup group) {
		this.group = group;
	}

	@Override
	public LocalGroup getGroup() {
		return this.group;
	}

	@Override
	public ServiceProviderRegistration<T, LocalGroupMember> register(T service) {
		this.services.add(service);
		return new DefaultServiceProviderRegistration<>(this, service, () -> this.services.remove(service));
	}

	@Override
	public ServiceProviderRegistration<T, LocalGroupMember> register(T service, ServiceProviderListener<LocalGroupMember> listener) {
		return this.register(service);
	}

	@Override
	public Set<LocalGroupMember> getProviders(T service) {
		return this.services.contains(service) ? Set.of(this.group.getLocalMember()) : Set.of();
	}

	@Override
	public Set<T> getServices() {
		return Collections.unmodifiableSet(this.services);
	}
}
