/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * {@link MarshalledValue} implementation suitable for map keys that uses a {@link ByteBufferMarshaller}.
 * @param <K> the key type
 * @author Paul Ferraro
 */
public class ByteBufferMarshalledKey<K> extends ByteBufferMarshalledValue<K> {
	private static final long serialVersionUID = 7317347779979133897L;

	/** The hash code of the marshalled value */
	private final int hashCode;

	/**
	 * Constructs a marshalled key using the specified object and marshaller.
	 * @param object a key
	 * @param marshaller a marshaller of the specified key
	 */
	public ByteBufferMarshalledKey(K object, ByteBufferMarshaller marshaller) {
		super(object, marshaller);
		this.hashCode = Objects.hashCode(object);
	}

	/**
	 * Constructs a marshalled key using the specified byte buffer and hash code.
	 * @param buffer a byte buffer
	 * @param hashCode the hash code of the key
	 */
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
