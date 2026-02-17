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
import java.util.concurrent.locks.StampedLock;

/**
 * {@link MarshalledValue} implementation that uses a {@link ByteBufferMarshaller}.
 * @author Paul Ferraro
 * @param <V> the type wrapped by this marshalled value
 */
public class ByteBufferMarshalledValue<V> implements MarshalledValue<V, ByteBufferMarshaller>, Serializable {
	private static final long serialVersionUID = -8419893544424515905L;

	/** Controls access to fields below */
	private final StampedLock lock = new StampedLock();

	private transient ByteBufferMarshaller marshaller;
	private transient V object;
	private transient ByteBuffer buffer;

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
		this.buffer = (buffer != null) ? duplicate(buffer) : null;
	}

	private static ByteBuffer duplicate(ByteBuffer buffer) {
		if (buffer.hasArray()) {
			return buffer.duplicate();
		}
		// If direct buffer, copy buffer contents
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return ByteBuffer.wrap(bytes);
	}

	// Used for testing purposes only
	V peek() {
		return this.object;
	}

	/**
	 * Indicates whether or not this value is empty.
	 * @return true, if this value is empty, false otherwise
	 */
	public boolean isEmpty() {
		long stamp = this.lock.tryOptimisticRead();
		try {
			boolean result = StampedLock.isOptimisticReadStamp(stamp) ? this.isEmptyUnsafe() : false;
			if (!this.lock.validate(stamp)) {
				// Optimistic read unsuccessful or invalid
				// Acquire pessimistic read lock
				stamp = this.lock.readLock();
				result = this.isEmptyUnsafe();
			}
			return result;
		} finally {
			if (StampedLock.isLockStamp(stamp)) {
				this.lock.unlock(stamp);
			}
		}
	}

	private boolean isEmptyUnsafe() {
		return (this.buffer == null) && (this.object == null);
	}

	/**
	 * Returns the byte buffer of this value, marshalling it if necessary.
	 * @return the byte buffer of this value.
	 * @throws IOException if the value could not be marshalled
	 */
	public ByteBuffer getBuffer() throws IOException {
		long stamp = this.lock.tryOptimisticRead();
		try {
			ByteBuffer result = StampedLock.isOptimisticReadStamp(stamp) ? this.getBufferUnsafe() : null;
			if (!this.lock.validate(stamp)) {
				// Optimistic read unsuccessful or invalid
				// Acquire pessimistic read lock
				stamp = this.lock.readLock();
				result = this.getBufferUnsafe();
			}
			return result;
		} finally {
			if (StampedLock.isLockStamp(stamp)) {
				this.lock.unlock(stamp);
			}
		}
	}

	private ByteBuffer getBufferUnsafe() throws IOException {
		// Since the wrapped object is likely mutable, we cannot cache a generated buffer
		ByteBuffer buffer = (this.buffer != null) ? this.buffer.duplicate() : (this.object != null) ? this.marshaller.write(this.object) : null;
		if ((buffer != null) && (this.buffer == null) && (this.object != null)) {
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
	public OptionalInt size() {
		long stamp = this.lock.tryOptimisticRead();
		try {
			OptionalInt result = StampedLock.isOptimisticReadStamp(stamp) ? this.sizeUnsafe() : null;
			if (!this.lock.validate(stamp)) {
				// Optimistic read unsuccessful or invalid
				// Acquire pessimistic read lock
				stamp = this.lock.readLock();
				result = this.sizeUnsafe();
			}
			return result;
		} finally {
			if (StampedLock.isLockStamp(stamp)) {
				this.lock.unlock(stamp);
			}
		}
	}

	private OptionalInt sizeUnsafe() {
		// N.B. Buffer position is guarded by synchronization on this object
		// We invalidate buffer upon reading it, ensuring that ByteBuffer.remaining() returns the effective buffer size
		return (this.buffer != null) ? OptionalInt.of(this.buffer.remaining()) : this.marshaller.size(this.object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(ByteBufferMarshaller marshaller) throws IOException {
		long stamp = this.lock.tryOptimisticRead();
		try {
			V result = this.object;
			ByteBuffer buffer = this.buffer;
			boolean unmarshal = (result == null) && (buffer != null);
			if (!this.lock.validate(stamp)) {
				stamp = this.lock.readLock();
				// Re-read with read lock
				result = this.object;
				buffer = this.buffer;
				unmarshal = (result == null) && (buffer != null);
			}
			if (unmarshal) {
				long conversionStamp = this.lock.tryConvertToWriteLock(stamp);
				if (StampedLock.isWriteLockStamp(conversionStamp)) {
					// Conversion successful
					stamp = conversionStamp;
				} else {
					// Conversion unsuccessful, release any pessimistic read lock and acquire write lock
					if (StampedLock.isReadLockStamp(stamp)) {
						this.lock.unlockRead(stamp);
					}
					stamp = this.lock.writeLock();
					// Re-read with write lock
					result = this.object;
					buffer = this.buffer;
					unmarshal = (result == null) && (buffer != null);
				}
				if (unmarshal) {
					result = (V) marshaller.read(buffer.duplicate());
					// Reference marshaller for use by writeObject(...)
					this.marshaller = marshaller;
					// Reference object
					this.object = result;
					// Invalidate buffer
					this.buffer = null;
				}
			}
			return result;
		} finally {
			if (StampedLock.isLockStamp(stamp)) {
				this.lock.unlock(stamp);
			}
		}
	}

	@Override
	public int hashCode() {
		long stamp = this.lock.tryOptimisticRead();
		try {
			int result = StampedLock.isOptimisticReadStamp(stamp) ? this.hashCodeUnsafe() : 0;
			if (!this.lock.validate(stamp)) {
				// Optimistic read unsuccessful or invalid
				// Acquire pessimistic read lock
				stamp = this.lock.readLock();
				result = this.hashCodeUnsafe();
			}
			return result;
		} finally {
			if (StampedLock.isLockStamp(stamp)) {
				this.lock.unlock(stamp);
			}
		}
	}

	private int hashCodeUnsafe() {
		return Objects.hashCode(this.object);
	}

	@Override
	public boolean equals(Object object) {
		long stamp = this.lock.tryOptimisticRead();
		try {
			boolean result = StampedLock.isOptimisticReadStamp(stamp) ? this.equalsUnsafe(object) : false;
			if (!this.lock.validate(stamp)) {
				// Optimistic read unsuccessful or invalid
				// Acquire pessimistic read lock
				stamp = this.lock.readLock();
				result = this.equalsUnsafe(object);
			}
			return result;
		} finally {
			if (StampedLock.isLockStamp(stamp)) {
				this.lock.unlock(stamp);
			}
		}
	}

	private boolean equalsUnsafe(Object object) {
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
		long stamp = this.lock.tryOptimisticRead();
		try {
			String result = StampedLock.isOptimisticReadStamp(stamp) ? this.toStringUnsafe() : null;
			if (!this.lock.validate(stamp)) {
				// Optimistic read unsuccessful or invalid
				// Acquire pessimistic read lock
				stamp = this.lock.readLock();
				result = this.toStringUnsafe();
			}
			return result;
		} finally {
			if (StampedLock.isLockStamp(stamp)) {
				this.lock.unlock(stamp);
			}
		}
	}

	private String toStringUnsafe() {
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
