/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

/**
 * An input stream for reading a byte buffer.
 * @author Paul Ferraro
 */
public class ByteBufferInputStream extends ByteArrayInputStream {

	/**
	 * Constructs an input stream for reading a byte buffer.
	 * @param buffer an array-backed byte buffer
	 */
	public ByteBufferInputStream(ByteBuffer buffer) {
		super(buffer.array(), buffer.arrayOffset(), buffer.limit() - buffer.arrayOffset());
	}
}
