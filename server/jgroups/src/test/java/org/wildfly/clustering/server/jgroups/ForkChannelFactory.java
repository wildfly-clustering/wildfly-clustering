/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.util.function.Function;

import org.jgroups.EmptyMessage;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.blocks.RequestCorrelator;
import org.jgroups.blocks.RequestCorrelator.Header;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.fork.ForkChannel;
import org.jgroups.fork.UnknownForkHandler;
import org.jgroups.protocols.FORK;

/**
 * @author Paul Ferraro
 */
public class ForkChannelFactory implements Function<String, JChannel> {

	private final JChannel channel;

	public ForkChannelFactory(JChannel channel) {
		FORK fork = new FORK();
		channel.getProtocolStack().addProtocol(fork.setUnknownForkHandler(new UnknownForkHandler() {
			private final short id = ClassConfigurator.getProtocolId(RequestCorrelator.class);

			@Override
			public Object handleUnknownForkStack(Message message, String forkStackId) {
				return this.handle(message);
			}

			@Override
			public Object handleUnknownForkChannel(Message message, String forkChannelId) {
				return this.handle(message);
			}

			private Object handle(Message message) {
				Header header = (Header) message.getHeader(this.id);
				// If this is a request expecting a response, don't leave the requester hanging - send an identifiable response on which it can filter
				if ((header != null) && (header.type == Header.REQ) && header.rspExpected()) {
					Message response = new EmptyMessage(message.src()).setFlag(message.getFlags(), false).clearFlag(Message.Flag.RSVP);
					if (message.getDest() != null) {
						response.src(message.getDest());
					}

					response.putHeader(FORK.ID, message.getHeader(FORK.ID));
					response.putHeader(this.id, new Header(Header.RSP, header.req_id, header.corrId));

					fork.getProtocolStack().getChannel().down(response);
				}
				return null;
			}
		}));
		this.channel = channel;
	}

	@Override
	public JChannel apply(String fork) {
		try {
			// Silence log messages when Infinispan calls ForkChannel.setName(...)
			return new ForkChannel(this.channel, this.channel.getClusterName(), fork) {
				@Override
				public ForkChannel setName(String name) {
					return this;
				}

				@Override
				public JChannel name(String name) {
					return this;
				}
			};
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
