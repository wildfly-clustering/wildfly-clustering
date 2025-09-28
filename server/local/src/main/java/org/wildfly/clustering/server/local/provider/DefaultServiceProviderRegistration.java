/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.provider;

import java.util.Set;

import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrar;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;

/**
 * A generic service provider registration implementation.
 * @param <T> the service provider type
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public class DefaultServiceProviderRegistration<T, M extends GroupMember> implements ServiceProviderRegistration<T, M> {

	private final ServiceProviderRegistrar<T, M> registrar;
	private final T service;
	private final Runnable closeTask;

	/**
	 * Creates a service provider registration for the specified service.
	 * @param registrar a service provider registrar
	 * @param service a service provider type
	 * @param closeTask a task to execute on registration close
	 */
	public DefaultServiceProviderRegistration(ServiceProviderRegistrar<T, M> registrar, T service, Runnable closeTask) {
		this.registrar = registrar;
		this.service = service;
		this.closeTask = closeTask;
	}

	@Override
	public T getService() {
		return this.service;
	}

	@Override
	public Set<M> getProviders() {
		return this.registrar.getProviders(this.service);
	}

	@Override
	public void close() {
		this.closeTask.run();
	}
}
