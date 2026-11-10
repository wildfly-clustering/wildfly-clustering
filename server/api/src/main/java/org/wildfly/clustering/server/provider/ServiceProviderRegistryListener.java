/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.provider;

import org.wildfly.clustering.server.GroupMember;

/**
 * A listener to be notified when the providers of a service change.
 * @author Paul Ferraro
 * @param <T> the service type
 * @param <M> the member type
 */
public interface ServiceProviderRegistryListener<T, M extends GroupMember> {
	/**
	 * Indicates that the providers of the given service have changed.
	 * @param service a provided service
	 * @param event a registration event
	 */
	void providersChanged(T service, ServiceProviderRegistrationEvent<M> event);
}
