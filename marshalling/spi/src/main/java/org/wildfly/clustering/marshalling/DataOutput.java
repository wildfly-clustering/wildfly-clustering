/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.IOException;

/**
 * Simplified {@link java.io.DataOutput} interface;
 * @author Paul Ferraro
 */
public interface DataOutput extends java.io.DataOutput {

	@Override
	default void write(int b) throws IOException {
		this.writeByte(b);
	}

	@Override
	default void write(byte[] bytes) throws IOException {
		this.write(bytes, 0, bytes.length);
	}

	@Override
	default void writeBytes(String value) throws IOException {
		this.write(value.getBytes());
	}

	@Override
	default void writeChars(String value) throws IOException {
		for (int i = 0; i < value.length(); ++i) {
			this.writeByte(value.charAt(i));
		}
	}
}
