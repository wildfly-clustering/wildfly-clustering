/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher;

import java.util.function.Predicate;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.wildfly.clustering.context.AbstractContext;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.server.jgroups.JChannelContext;

/**
 * @author Paul Ferraro
 */
public class ChannelCommandDispatcherFactoryContext extends AbstractContext<ChannelCommandDispatcherFactory> {

	private final ChannelCommandDispatcherFactory factory;

	public ChannelCommandDispatcherFactoryContext(String clusterName, String memberName) {
		try {
			Context<JChannel> channel = new JChannelContext(clusterName, memberName);
			this.accept(channel::close);
			ByteBufferMarshaller marshaller = new ProtoStreamTesterFactory().getMarshaller();
			this.factory = new JChannelCommandDispatcherFactory(new JChannelCommandDispatcherFactoryConfiguration() {
				@Override
				public Predicate<Message> getUnknownForkPredicate() {
					return Predicate.not(Message::hasPayload);
				}

				@Override
				public JChannel getChannel() {
					return channel.get();
				}

				@Override
				public ByteBufferMarshaller getMarshaller() {
					return marshaller;
				}

				@Override
				public java.util.function.Function<ClassLoader, ByteBufferMarshaller> getMarshallerFactory() {
					return Function.of(marshaller);
				}
			});
			this.accept(this.factory::close);
		} catch (RuntimeException | Error e) {
			this.close();
			throw e;
		}
	}

	@Override
	public ChannelCommandDispatcherFactory get() {
		return this.factory;
	}
}
