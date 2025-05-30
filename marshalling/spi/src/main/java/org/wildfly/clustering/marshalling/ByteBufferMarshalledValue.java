/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * {@link MarshalledValue} implementation that uses a {@link ByteBufferMarshaller}.
 * @author Paul Ferraro
 * @param <V> the type wrapped by this marshalled value
 */
public class ByteBufferMarshalledValue<V> implements MarshalledValue<V, ByteBufferMarshaller>, Serializable {
	private static final long serialVersionUID = -8419893544424515905L;

	private transient volatile ByteBufferMarshaller marshaller;
	private transient volatile V object;
	private transient volatile ByteBuffer buffer;

	/**
	 * Constructs a marshalled value from the specified object and marshaller.
	 * @param object the wrapped object
	 * @param marshaller a marshaller suitable for marshalling the specified object
	 */
	public ByteBufferMarshalledValue(V object, ByteBufferMarshaller marshaller) {
		this.marshaller = marshaller;
		this.object = object;
	}

	/**
	 * Constructs a marshalled value from the specified byte buffer.
	 * This constructor is only public to facilitate marshallers of this object (from other packages).
	 * The byte buffer parameter must not be read outside the context of this object.
	 * @param buffer a byte buffer
	 */
	public ByteBufferMarshalledValue(ByteBuffer buffer) {
		// Normally, we would create a defensive ByteBuffer.asReadOnlyBuffer()
		// but this would preclude the use of operations on the backing array.
		this.buffer = buffer;
	}

	// Used for testing purposes only
	V peek() {
		return this.object;
	}

	public synchronized boolean isEmpty() {
		return (this.buffer == null) && (this.object == null);
	}

	public synchronized ByteBuffer getBuffer() throws IOException {
		ByteBuffer buffer = this.buffer;
		if ((buffer == null) && (this.object != null)) {
			// Since the wrapped object is likely mutable, we cannot cache the generated buffer
			buffer = this.marshaller.write(this.object);
			// N.B. Refrain from logging wrapped object
			// If wrapped object contains an EJB proxy, toString() will trigger an EJB invocation!
			Logger.INSTANCE.log(System.Logger.Level.DEBUG, "Marshalled size of {0} object = {1} bytes", this.object.getClass().getCanonicalName(), buffer.limit() - buffer.arrayOffset());
		}
		return buffer;
	}

	public synchronized OptionalInt size() {
		// N.B. Buffer position is guarded by synchronization on this object
		// We invalidate buffer upon reading it, ensuring that ByteBuffer.remaining() returns the effective buffer size
		return (this.buffer != null) ? OptionalInt.of(this.buffer.remaining()) : this.marshaller.size(this.object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized V get(ByteBufferMarshaller marshaller) throws IOException {
		if (this.object == null) {
			this.marshaller = marshaller;
			if (this.buffer != null) {
				// Invalidate buffer after reading object
				this.object = (V) this.marshaller.read(this.buffer);
				this.buffer = null;
			}
		}
		return this.object;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.object);
	}

	@Override
	public boolean equals(Object object) {
		if ((object == null) || !(object instanceof ByteBufferMarshalledValue)) return false;
		@SuppressWarnings("unchecked")
		ByteBufferMarshalledValue<V> value = (ByteBufferMarshalledValue<V>) object;
		Object ourObject = this.object;
		Object theirObject = value.object;
		if ((ourObject != null) && (theirObject != null)) {
			return ourObject.equals(theirObject);
		}
		try {
			ByteBuffer us = this.getBuffer();
			ByteBuffer them = value.getBuffer();
			return ((us != null) && (them != null)) ? us.equals(them) : (us == them);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		// N.B. Refrain from logging wrapped object
		// If wrapped object contains an EJB proxy, toString() will trigger an EJB invocation!
		return String.format("%s [%s]", this.getClass().getName(), (this.object != null) ? this.object.getClass().getName() : "<serialized>");
	}

	private void writeObject(ObjectOutputStream output) throws IOException {
		output.defaultWriteObject();
		ByteBuffer buffer = this.getBuffer();
		int length = (buffer != null) ? buffer.limit() - buffer.arrayOffset() : 0;
		IndexSerializer.VARIABLE.writeInt(output, length);
		if (length > 0) {
			output.write(buffer.array(), buffer.arrayOffset(), length);
		}
	}

	private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
		input.defaultReadObject();
		int size = IndexSerializer.VARIABLE.readInt(input);
		byte[] bytes = (size > 0) ? new byte[size] : null;
		if (bytes != null) {
			input.readFully(bytes);
		}
		this.buffer = (bytes != null) ? ByteBuffer.wrap(bytes) : null;
	}
}
