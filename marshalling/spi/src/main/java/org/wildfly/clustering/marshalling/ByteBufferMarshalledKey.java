/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * {@link MarshalledValue} implementation suitable for map keys that uses a {@link ByteBufferMarshaller}.
 * @author Paul Ferraro
 */
public class ByteBufferMarshalledKey<T> extends ByteBufferMarshalledValue<T> {
	private static final long serialVersionUID = 7317347779979133897L;

	private final int hashCode;

	public ByteBufferMarshalledKey(T object, ByteBufferMarshaller marshaller) {
		super(object, marshaller);
		this.hashCode = Objects.hashCode(object);
	}

	public ByteBufferMarshalledKey(ByteBuffer buffer, int hashCode) {
		super(buffer);
		this.hashCode = hashCode;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object object) {
		// Optimize by verifying equality of hash code first
		if ((object == null) || !(object instanceof ByteBufferMarshalledKey) || (this.hashCode != object.hashCode())) return false;
		return super.equals(object);
	}
}
