/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Map;

import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshalledValue;
import org.wildfly.clustering.marshalling.MarshalledValueFactory;
import org.wildfly.clustering.server.dispatcher.Command;

/**
 * Marshaller for commands.
 * @param <C> the command execution context
 * @param <MC> the marshalling context
 * @author Paul Ferraro
 */
public class CommandDispatcherMarshaller<C, MC> implements CommandMarshaller<C> {

	private final ByteBufferMarshaller marshaller;
	private final Object id;
	private final MarshalledValueFactory<MC> factory;

	public CommandDispatcherMarshaller(ByteBufferMarshaller marshaller, Object id, MarshalledValueFactory<MC> factory) {
		this.marshaller = marshaller;
		this.id = id;
		this.factory = factory;
	}

	@Override
	public <R, E extends Exception> ByteBuffer marshal(Command<R, ? super C, E> command) throws IOException {
		MarshalledValue<Command<R, ? super C, E>, MC> value = this.factory.createMarshalledValue(command);
		Map.Entry<Object, MarshalledValue<Command<R, ? super C, E>, MC>> entry = new AbstractMap.SimpleImmutableEntry<>(this.id, value);
		return this.marshaller.write(entry);
	}
}
