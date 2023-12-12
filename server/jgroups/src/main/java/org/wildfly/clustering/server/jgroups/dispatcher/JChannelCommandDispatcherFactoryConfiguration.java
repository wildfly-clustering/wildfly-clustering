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

/**
 * Configuration for a {@link JChannelCommandDispatcherFactory}.
 * @author Paul Ferraro
 */
public interface JChannelCommandDispatcherFactoryConfiguration {
	Predicate<Message> getUnknownForkPredicate();
	JChannel getChannel();
	ByteBufferMarshaller getMarshaller();
	Function<ClassLoader, ByteBufferMarshaller> getMarshallerFactory();
}
