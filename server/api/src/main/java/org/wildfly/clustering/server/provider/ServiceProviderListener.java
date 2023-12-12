/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.provider;

import java.util.Set;

import org.wildfly.clustering.server.GroupMember;

/**
 * A listener that is notified when the set of providers for a given service changes.
 * @author Paul Ferraro
 */
public interface ServiceProviderListener<M extends GroupMember> {
	/**
	 * Indicates that the set of group members providing a given service has changed.
	 *
	 * @param providers the new set of group members providing the given service
	 */
	void providersChanged(Set<M> providers);
}
