/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import org.jgroups.JChannel;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * @param <A> the address type of the group member
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public interface GroupProvider<A extends Comparable<A>, M extends GroupMember<A>> extends AutoCloseable {

	JChannel getChannel();

	Group<A, M> getGroup();

	@Override
	void close();
}
