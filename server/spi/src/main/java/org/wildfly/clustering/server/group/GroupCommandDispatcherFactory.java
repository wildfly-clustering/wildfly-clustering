/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.group;

import org.wildfly.clustering.server.dispatcher.CommandDispatcherFactory;

/**
 * {@link CommandDispatcherFactory} whose group exposes a mechanism to create a group member for a given unique address.
 * @param <A> the group member address type
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public interface GroupCommandDispatcherFactory<A extends Comparable<A>, M extends GroupMember<A>> extends CommandDispatcherFactory<M> {

	@Override
	Group<A, M> getGroup();
}
