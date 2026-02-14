/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import org.jgroups.JChannel;
import org.wildfly.clustering.context.AbstractContext;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.function.Runner;

/**
 * @author Paul Ferraro
 */
public class ChannelGroupContext extends AbstractContext<ChannelGroup> {

	private final ChannelGroup group;

	public ChannelGroupContext(String clusterName, String memberName) {
		this(new JChannelContext(clusterName, memberName));
	}

	public ChannelGroupContext(JChannel channel) {
		this(Context.of(channel, Runner.of()));
	}

	private ChannelGroupContext(Context<JChannel> channel) {
		try {
			this.accept(channel::close);
			this.group = new JChannelGroup(channel.get());
			this.accept(this.group::close);
		} catch (RuntimeException | Error e) {
			this.close();
			throw e;
		}
	}

	@Override
	public ChannelGroup get() {
		return this.group;
	}
}
