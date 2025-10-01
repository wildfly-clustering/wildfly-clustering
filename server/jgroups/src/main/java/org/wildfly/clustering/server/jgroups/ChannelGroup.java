/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.wildfly.clustering.server.group.Group;

/**
 * A channel-based group whose membership is based on the channel view.
 * @author Paul Ferraro
 */
public interface ChannelGroup extends Group<Address, ChannelGroupMember>, AutoCloseable {

	/**
	 * Returns the channel associated with this group.
	 * @return the channel associated with this group.
	 */
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
