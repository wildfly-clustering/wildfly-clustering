/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.wildfly.clustering.server.group.Group;

/**
 * @author Paul Ferraro
 */
public interface ChannelGroup extends Group<Address, ChannelGroupMember>, AutoCloseable {

	JChannel getChannel();

	@Override
	default String getName() {
		return this.getChannel().getClusterName();
	}

	@Override
	default boolean isSingleton() {
		return false;
	}

	@Override
	ChannelGroupMemberFactory getGroupMemberFactory();

	@Override
	void close();
}
