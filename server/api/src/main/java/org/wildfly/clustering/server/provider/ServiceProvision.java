/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.provider;

import java.util.Set;

import org.wildfly.clustering.server.GroupMember;

/**
 * Encapsulates the state of a provisioned service.
 *
 * @author Paul Ferraro
 * @param <T> the service type
 * @param <M> the member type
 */
public interface ServiceProvision<T, M extends GroupMember> extends ServiceProvider<T> {

	/**
	 * Returns an unmodifiable set of group members currently providing this service.
	 * @return an unmodifiable set of group members currently providing this service.
	 */
	Set<M> getProviders();
}
