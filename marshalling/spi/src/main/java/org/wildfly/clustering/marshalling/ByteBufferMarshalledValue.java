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
		this.buffer = (buffer != null) ? buffer.duplicate() : null;
	}

	// Used for testing purposes only
	V peek() {
		return this.object;
	}

	/**
	 * Indicates whether or not this value is empty.
	 * @return true, if this value is empty, false otherwise
	 */
	public synchronized boolean isEmpty() {
		return (this.buffer == null) && (this.object == null);
	}

	/**
	 * Returns the byte buffer of this value, marshalling it if necessary.
	 * @return the byte buffer of this value.
	 * @throws IOException if the value could not be marshalled
	 */
	public synchronized ByteBuffer getBuffer() throws IOException {
		ByteBuffer buffer = this.buffer;
		if ((buffer == null) && (this.object != null)) {
			// Since the wrapped object is likely mutable, we cannot cache the generated buffer
			buffer = this.marshaller.write(this.object);
			// N.B. Refrain from logging wrapped object
			// If wrapped object contains an EJB proxy, toString() will trigger an EJB invocation!
			Logger.INSTANCE.log(System.Logger.Level.TRACE, "Marshalled size of {0} object = {1} bytes", this.object.getClass().getCanonicalName(), buffer.remaining());
		}
		return buffer;
	}

	/**
	 * If present, returns the size of the buffer returned by {@link #getBuffer()}.
	 * @return an optional buffer size
	 */
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
		if ((object == null) || !(object instanceof ByteBufferMarshalledValue value)) return false;
		Object ourObject = this.object;
		Object theirObject = value.object;
		if ((ourObject != null) && (theirObject != null)) {
			return ourObject.equals(theirObject);
		}
		try {
			return Objects.equals(this.getBuffer(), value.getBuffer());
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

	/**
	 * Writes this object to the specified output stream.
	 * @param output an output stream
	 * @throws IOException if the fields of this object could not be written
	 */
	private void writeObject(ObjectOutputStream output) throws IOException {
		output.defaultWriteObject();
		ByteBuffer buffer = this.getBuffer();
		int length = (buffer != null) ? buffer.remaining() : 0;
		IndexSerializer.VARIABLE.writeInt(output, length);
		if (length > 0) {
			if (buffer.hasArray()) {
				output.write(buffer.array(), buffer.arrayOffset() + buffer.position(), length);
			} else {
				ByteBuffer duplicate = buffer.duplicate();
				byte[] chunk = new byte[Math.min(length, Byte.MAX_VALUE + 1)];
				while (duplicate.hasRemaining()) {
					int bytes = Math.min(duplicate.remaining(), chunk.length);
					duplicate.get(chunk, 0, bytes);
					output.write(chunk, 0, bytes);
				}
			}
		}
	}

	/**
	 * Reads this object from the specified input stream.
	 * @param input an input stream
	 * @throws IOException if the fields of this object could not be read
	 * @throws ClassNotFoundException if this class could not be loaded by the class loader of the current thread context
	 */
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
