/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * An input stream for reading a byte buffer.
 * @author Paul Ferraro
 */
public class ByteBufferInputStream extends InputStream {
	private final ByteBuffer buffer;

	/**
	 * Constructs an input stream for reading a byte buffer.
	 * @param buffer a byte buffer
	 */
	public ByteBufferInputStream(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public int read() {
		return this.buffer.hasRemaining() ? Byte.toUnsignedInt(this.buffer.get()) : -1;
	}

	@Override
	public int read(byte[] bytes, int offset, int length) {
		if (length == 0) return 0;
		int count = this.buffer.hasRemaining() ? Math.min(length, this.buffer.remaining()) : -1;
		if (count > 0) {
			this.buffer.get(bytes, offset, count);
		}
		return count;
	}

	@Override
	public int available() {
		return this.buffer.remaining();
	}

	@Override
	public long skip(long bytes) {
		int offset = (int) Math.min((long) this.buffer.remaining(), bytes);
		this.buffer.position(this.buffer.position() + offset);
		return offset;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public void mark(int readlimit) {
		this.buffer.mark();
	}

	@Override
	public void reset() {
		this.buffer.reset();
	}
}
