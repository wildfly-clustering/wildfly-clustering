/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.provider;

import org.wildfly.clustering.server.GroupMember;

/**
 * A listener to be notified when the providers of a given service change.
 * @author Paul Ferraro
 * @param <M> the member type
 */
public interface ServiceProviderRegistrationListener<M extends GroupMember> {
	/**
	 * Indicates that the providers of a given service have changed.
	 * @param event a registration event
	 */
	void providersChanged(ServiceProviderRegistrationEvent<M> event);
}
