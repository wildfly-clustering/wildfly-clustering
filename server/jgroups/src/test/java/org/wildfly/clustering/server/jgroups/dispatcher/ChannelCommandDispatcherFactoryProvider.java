/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher;

import java.util.function.Function;
import java.util.function.Predicate;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.wildfly.clustering.cache.function.Functions;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.server.AutoCloseableProvider;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;
import org.wildfly.clustering.server.jgroups.JChannelFactory;

/**
 * @author Paul Ferraro
 */
public class ChannelCommandDispatcherFactoryProvider extends AutoCloseableProvider implements CommandDispatcherFactoryProvider<ChannelGroupMember>, JChannelCommandDispatcherFactoryConfiguration {

	private final ByteBufferMarshaller marshaller = new ProtoStreamTesterFactory().getMarshaller();
	private final JChannel channel;
	private final ChannelCommandDispatcherFactory factory;

	public ChannelCommandDispatcherFactoryProvider(String clusterName, String memberName) throws Exception {
		this.channel = JChannelFactory.INSTANCE.apply(memberName);
		this.accept(this.channel::close);
		this.channel.connect(clusterName);
		this.accept(this.channel::disconnect);
		this.factory = new JChannelCommandDispatcherFactory(this);
		this.accept(this.factory::close);
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
		return this.marshaller;
	}

	@Override
	public Function<ClassLoader, ByteBufferMarshaller> getMarshallerFactory() {
		return Functions.constantFunction(this.marshaller);
	}
}
