/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

/**
 * Unit tests for {@link ByteBufferMarshalledValue}.
 *
 * @author Brian Stansberry
 * @author Paul Ferraro
 */
public abstract class ByteBufferMarshalledKeyFactoryTestCase extends ByteBufferMarshalledValueFactoryTestCase {

	private final ByteBufferMarshalledKeyFactory factory;

	protected ByteBufferMarshalledKeyFactoryTestCase(ByteBufferMarshaller marshaller) {
		this(marshaller, new ByteBufferMarshalledKeyFactory(marshaller));
	}

	private ByteBufferMarshalledKeyFactoryTestCase(ByteBufferMarshaller marshaller, ByteBufferMarshalledKeyFactory factory) {
		super(marshaller, factory);
		this.factory = factory;
	}

	@Override
	public void testHashCode() throws Exception {
		UUID uuid = UUID.randomUUID();
		int expected = uuid.hashCode();
		ByteBufferMarshalledValue<UUID> mv = this.factory.createMarshalledValue(uuid);
		assertEquals(expected, mv.hashCode());

		ByteBufferMarshalledValue<UUID> copy = replicate(mv);
		assertEquals(expected, copy.hashCode());

		mv = this.factory.createMarshalledValue(null);
		assertEquals(0, mv.hashCode());
	}
}
