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
public class JChannelGroupITCaseConfiguration implements GroupITCaseConfiguration<Address, ChannelGroupMember> {

	private final JChannel channel;
	private final ChannelGroup group;

	public JChannelGroupITCaseConfiguration(String clusterName, String memberName) throws Exception {
		this.channel = JChannelFactory.INSTANCE.apply(memberName);
		this.channel.connect(clusterName);
		this.group = new JChannelGroup(this.channel);
	}

	@Override
	public JChannel getChannel() {
		return this.channel;
	}

	@Override
	public Group<Address, ChannelGroupMember> getGroup() {
		return this.group;
	}

	@Override
	public String getName() {
		return this.channel.getClusterName();
	}

	@Override
	public void close() throws Exception {
		this.group.close();
		this.channel.disconnect();
		this.channel.close();
	}
}
