/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.provider;

import java.util.Set;

import org.wildfly.clustering.server.GroupMember;

/**
 * Encapsulates a provisioned service.
 *
 * @author Paul Ferraro
 * @param <T> the service type
 * @param <M> the member type
 */
public interface ServiceProvision<T, M extends GroupMember> {

	/**
	 * Returns the provided service.
	 * @return the provided service.
	 */
	T getService();

	/**
	 * Returns an unmodifiable set of group members currently providing this service.
	 * @return an unmodifiable set of group members currently providing this service.
	 */
	Set<M> getProviders();
}
