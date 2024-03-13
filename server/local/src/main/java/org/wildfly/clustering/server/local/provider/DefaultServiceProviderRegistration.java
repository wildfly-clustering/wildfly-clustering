/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.provider;

import java.util.Set;

import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;
import org.wildfly.clustering.server.provider.ServiceProviderRegistry;

/**
 * A generic service provider registration implementation.
 * @param <T> the service provider type
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public class DefaultServiceProviderRegistration<T, M extends GroupMember> implements ServiceProviderRegistration<T, M> {

	private final ServiceProviderRegistry<T, M> registry;
	private final T service;
	private final Runnable closeTask;

	public DefaultServiceProviderRegistration(ServiceProviderRegistry<T, M> registry, T service, Runnable closeTask) {
		this.registry = registry;
		this.service = service;
		this.closeTask = closeTask;
	}

	@Override
	public T getService() {
		return this.service;
	}

	@Override
	public Set<M> getProviders() {
		return this.registry.getProviders(this.service);
	}

	@Override
	public void close() {
		this.closeTask.run();
	}
}
