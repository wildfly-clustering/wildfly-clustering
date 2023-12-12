/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher;

import java.util.function.Function;
import java.util.function.Predicate;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;
import org.wildfly.clustering.server.jgroups.JChannelFactory;

/**
 * @author Paul Ferraro
 */
public class ChannelCommandDispatcherITCaseConfiguration implements CommandDispatcherITCaseConfiguration<ChannelGroupMember>, JChannelCommandDispatcherFactoryConfiguration {
	static final ByteBufferMarshaller MARSHALLER = new ProtoStreamTesterFactory().get();

	private final JChannel channel;
	private final ChannelCommandDispatcherFactory factory;

	public ChannelCommandDispatcherITCaseConfiguration(String clusterName, String memberName) throws Exception {
		this.channel = JChannelFactory.INSTANCE.apply(memberName);
		this.channel.connect(clusterName);
		this.factory = new JChannelCommandDispatcherFactory(this);
	}

	@Override
	public ChannelCommandDispatcherFactory getCommandDispatcherFactory() {
		return this.factory;
	}

	@Override
	public Predicate<Message> getUnknownForkPredicate() {
		return Predicate.not(Message::hasPayload);
	}

	@Override
	public JChannel getChannel() {
		return this.channel;
	}

	@Override
	public ByteBufferMarshaller getMarshaller() {
		return MARSHALLER;
	}

	@Override
	public Function<ClassLoader, ByteBufferMarshaller> getMarshallerFactory() {
		return loader -> MARSHALLER;
	}

	@Override
	public void close() throws Exception {
		this.factory.close();
		this.channel.close();
	}
}
