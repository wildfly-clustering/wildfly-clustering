/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

/**
 * Factory for creating a {@link ByteBufferMarshalledValue}.
 * @author Paul Ferraro
 */
public class ByteBufferMarshalledValueFactory implements MarshalledValueFactory<ByteBufferMarshaller> {

	private final ByteBufferMarshaller marshaller;

	public ByteBufferMarshalledValueFactory(ByteBufferMarshaller marshaller) {
		this.marshaller = marshaller;
	}

	@Override
	public boolean isMarshallable(Object object) {
		return this.marshaller.isMarshallable(object);
	}

	@Override
	public <V> ByteBufferMarshalledValue<V> createMarshalledValue(V object) {
		return new ByteBufferMarshalledValue<>(object, this.marshaller);
	}

	@Override
	public ByteBufferMarshaller getMarshallingContext() {
		return this.marshaller;
	}
}
