/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.net.InetAddress;
import java.util.function.Function;

import org.jgroups.JChannel;

/**
 * @author Paul Ferraro
 */
public enum JChannelFactory implements Function<String, JChannel> {
	INSTANCE;

	static final String JGROUPS_CONFIG = "jgroups.xml";

	static {
		System.setProperty("jgroups.bind_addr", InetAddress.getLoopbackAddress().getHostAddress());
	}

	@Override
	public JChannel apply(String memberName) {
		try {
			JChannel channel = new JChannel(JGROUPS_CONFIG);
			channel.setName(memberName);
			return channel;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
