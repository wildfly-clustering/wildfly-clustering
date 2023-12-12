/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.provider;

import java.util.Set;

import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.Registration;

/**
 * Registration of a provided service.
 *
 * @author Paul Ferraro
 * @param <T> the service type
 * @param <M> the member type
 */
public interface ServiceProviderRegistration<T, M extends GroupMember> extends Registration {

	/**
	 * The provided service.
	 * @return a service identifier
	 */
	T getService();

	/**
	 * Returns the set of group members that can provide this service.
	 * @return a set of group members
	 */
	Set<M> getProviders();
}
