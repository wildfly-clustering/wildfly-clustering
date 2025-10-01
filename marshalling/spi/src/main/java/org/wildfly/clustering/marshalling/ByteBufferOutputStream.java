/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.OptionalInt;

/**
 * An output stream for writing to a byte buffer.
 * @author Paul Ferraro
 */
public final class ByteBufferOutputStream extends ByteArrayOutputStream {
	private static final int DEFAULT_INITIAL_CAPACITY = 512;

	/**
	 * Constructs a new output stream with an default initial capacity.
	 */
	public ByteBufferOutputStream() {
		this(OptionalInt.empty());
	}

	/**
	 * Constructs a new output stream with an optional initial capacity.
	 * @param capacity an optional initial capacity
	 */
	public ByteBufferOutputStream(OptionalInt capacity) {
		this(capacity.orElse(DEFAULT_INITIAL_CAPACITY));
	}

	/**
	 * Constructs a new output stream with the specified initial capacity.
	 * @param capacity the initial capacity
	 */
	public ByteBufferOutputStream(int capacity) {
		super(capacity);
	}

	/**
	 * Returns the internal buffer of this output stream.
	 * @return the internal byte buffer.
	 */
	public ByteBuffer getBuffer() {
		return ByteBuffer.wrap(this.buf, 0, this.count);
	}
}
