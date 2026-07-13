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
	 * Returns an empty listener.
	 * @param <M> the group member type
	 * @return an empty listener.
	 */
	static <M extends GroupMember> ServiceProviderRegistrationListener<M> of() {
		return new ServiceProviderRegistrationListener<>() {
			@Override
			public void providersChanged(ServiceProviderRegistrationEvent<M> event) {
				// Do nothing
			}
		};
	}

	/**
	 * Indicates that the providers of a given service have changed.
	 * @param event a registration event
	 */
	void providersChanged(ServiceProviderRegistrationEvent<M> event);
}
