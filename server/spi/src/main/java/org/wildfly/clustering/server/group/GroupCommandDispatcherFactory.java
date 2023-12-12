/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.group;

import org.wildfly.clustering.server.dispatcher.CommandDispatcherFactory;

/**
 * @author Paul Ferraro
 */
public interface GroupCommandDispatcherFactory<A extends Comparable<A>, M extends GroupMember<A>> extends CommandDispatcherFactory<M> {

	@Override
	Group<A, M> getGroup();
}
