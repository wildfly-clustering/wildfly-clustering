/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.EOFException;
import java.io.IOException;

/**
 * Simplified {@link java.io.DataInput} interface.
 * @author Paul Ferraro
 */
public interface DataInput extends java.io.DataInput {

	@Override
	default void readFully(byte[] bytes) throws IOException {
		this.readFully(bytes, 0, bytes.length);
	}

	@Override
	default char readChar() throws IOException {
		return (char) this.readUnsignedShort();
	}

	@Override
	default String readLine() throws IOException {
		return this.readUTF();
	}

	@Override
	default int skipBytes(int size) throws IOException {
		for (int i = 0; i < size; ++i) {
			try {
				this.readByte();
			} catch (EOFException e) {
				return i;
			}
		}
		return size;
	}
}
