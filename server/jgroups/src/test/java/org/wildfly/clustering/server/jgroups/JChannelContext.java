/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import org.jgroups.JChannel;
import org.wildfly.clustering.context.AbstractContext;

/**
 * A context providing a {@link JChannel}.
 * @author Paul Ferraro
 */
public class JChannelContext extends AbstractContext<JChannel> {

	private final JChannel channel;

	public JChannelContext(String clusterName, String memberName) {
		this.channel = JChannelFactory.INSTANCE.apply(memberName);
		this.accept(this.channel::close);
		try {
			this.channel.connect(clusterName);
			this.accept(this.channel::disconnect);
		} catch (RuntimeException | Error e) {
			this.close();
			throw e;
		} catch (Exception e) {
			this.close();
			throw new RuntimeException(e);
		}
	}

	@Override
	public JChannel get() {
		return this.channel;
	}
}
