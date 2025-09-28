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
	/**
	 * Returns a predicate that determines whether a given message is associated with an unknown fork channel.
	 * @return a predicate that determines whether a given message is associated with an unknown fork channel.
	 */
	Predicate<Message> getUnknownForkPredicate();

	/**
	 * Returns the channel associated with this command dispatcher factory.
	 * @return the channel associated with this command dispatcher factory.
	 */
	JChannel getChannel();

	/**
	 * Returns the marshaller associated with this command dispatcher factory.
	 * @return the marshaller associated with this command dispatcher factory.
	 */
	ByteBufferMarshaller getMarshaller();

	/**
	 * Returns a factory for creating command dispatcher specific marshaller.
	 * @return a factory for creating command dispatcher specific marshaller.
	 */
	Function<ClassLoader, ByteBufferMarshaller> getMarshallerFactory();
}
