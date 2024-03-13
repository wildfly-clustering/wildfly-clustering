/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

/**
 * Factory for creating a {@link ByteBufferMarshalledKey}.
 * @author Paul Ferraro
 */
public class ByteBufferMarshalledKeyFactory extends ByteBufferMarshalledValueFactory {

	private final ByteBufferMarshaller marshaller;

	public ByteBufferMarshalledKeyFactory(ByteBufferMarshaller marshaller) {
		super(marshaller);
		this.marshaller = marshaller;
	}

	@Override
	public <K> ByteBufferMarshalledKey<K> createMarshalledValue(K object) {
		return new ByteBufferMarshalledKey<>(object, this.marshaller);
	}
}
