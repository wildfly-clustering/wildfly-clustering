/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.local.provider;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.wildfly.clustering.server.Group;
import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.provider.ServiceProviderListener;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrar;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;

/**
 * Factory that provides a non-clustered {@link ServiceProviderRegistration} implementation.
 * @param <T> the service provider type
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public class LocalServiceProviderRegistrar<T, M extends GroupMember> implements ServiceProviderRegistrar<T, M> {

	private final Set<T> services = ConcurrentHashMap.newKeySet();
	private final Group<M> group;

	public LocalServiceProviderRegistrar(Group<M> group) {
		this.group = group;
	}

	@Override
	public Group<M> getGroup() {
		return this.group;
	}

	@Override
	public ServiceProviderRegistration<T, M> register(T service) {
		this.services.add(service);
		return new DefaultServiceProviderRegistration<>(this, service, () -> this.services.remove(service));
	}

	@Override
	public ServiceProviderRegistration<T, M> register(T service, ServiceProviderListener<M> listener) {
		return this.register(service);
	}

	@Override
	public Set<M> getProviders(T service) {
		return this.services.contains(service) ? Set.of(this.group.getLocalMember()) : Set.of();
	}

	@Override
	public Set<T> getServices() {
		return Collections.unmodifiableSet(this.services);
	}
}
