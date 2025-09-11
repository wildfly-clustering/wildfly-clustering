/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.provider;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.wildfly.clustering.server.GroupMember;

/**
 * Encapsulates a service provider registration event.
 * @author Paul Ferraro
 * @param <M> the member type
 */
public interface ServiceProviderRegistrationEvent<M extends GroupMember> {

	/**
	 * Returns an unmodifiable set of group members previously providing this service.
	 * @return an unmodifiable set of group members previously providing this service.
	 */
	Set<M> getPreviousProviders();

	/**
	 * Returns an unmodifiable set of group members currently providing this service.
	 * @return an unmodifiable set of group members currently providing this service.
	 */
	Set<M> getCurrentProviders();

	/**
	 * Returns an unmodifiable set of group members that no longer provide this service.
	 * @return an unmodifiable set of group members that no longer provide this service.
	 */
	default Set<M> getObsoleteProviders() {
		Set<M> members = new HashSet<>(this.getPreviousProviders());
		members.removeAll(this.getCurrentProviders());
		return Collections.unmodifiableSet(members);
	}

	/**
	 * Returns an unmodifiable set of group members that newly provide this service.
	 * @return an unmodifiable set of group members that newly provide this service.
	 */
	default Set<M> getNewProviders() {
		Set<M> members = new HashSet<>(this.getCurrentProviders());
		members.removeAll(this.getPreviousProviders());
		return Collections.unmodifiableSet(members);
	}
}
