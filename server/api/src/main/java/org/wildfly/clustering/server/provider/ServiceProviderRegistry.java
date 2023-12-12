/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.provider;

import java.util.Set;

import org.wildfly.clustering.server.Group;
import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.Registrar;

/**
 * A distributed registry of service providers.
 * Allows a client to query the group members that can provide a given service.
 * @author Paul Ferraro
 * @param <T> the service type
 * @param <M> the member type
 */
public interface ServiceProviderRegistry<T, M extends GroupMember> extends Registrar<T> {

	/**
	 * Returns the group with which to register service providers.
	 *
	 * @return a group
	 */
	Group<M> getGroup();

	/**
	 * Registers the local group member as a provider of the specified service.
	 *
	 * @param service a service to register
	 * @return a new service provider registration
	 */
	@Override
	ServiceProviderRegistration<T, M> register(T service);

	/**
	 * Registers the local group member as a provider of the specified service, using the specified listener.
	 *
	 * @param service a service to register
	 * @param listener a registry listener
	 * @return a new service provider registration
	 */
	ServiceProviderRegistration<T, M> register(T service, ServiceProviderListener<M> listener);

	/**
	 * Returns the set of group members that can provide the specified service.
	 *
	 * @param service a service for which to obtain providers
	 * @return a set of group members providing the specified service
	 */
	Set<M> getProviders(T service);

	/**
	 * Returns the complete list of services known to this registry.
	 * @return a set of services
	 */
	Set<T> getServices();
}
