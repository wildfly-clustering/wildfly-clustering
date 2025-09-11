/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.provider;

import java.util.Set;

import org.wildfly.clustering.server.Group;
import org.wildfly.clustering.server.GroupMember;

/**
 * A distributed registry of service providers.
 * @author Paul Ferraro
 * @param <T> the service type
 * @param <M> the member type
 */
public interface ServiceProviderRegistry<T, M extends GroupMember> {
	/**
	 * Returns the group to associated with this registry.
	 *
	 * @return a group
	 */
	Group<M> getGroup();

	/**
	 * Returns an unmodifiable set of group members providing the specified service
	 *
	 * @param service a service identifier
	 * @return an unmodifiable set of group members providing the specified service
	 */
	Set<M> getProviders(T service);

	/**
	 * Returns an unmodifiable set of services for which providers exist.
	 * @return an unmodifiable set of services for which providers exist.
	 */
	Set<T> getServices();
}
