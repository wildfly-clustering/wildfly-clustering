/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import java.util.List;

import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.ByteBufferTestMarshaller;
import org.wildfly.clustering.marshalling.MarshallingTester;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;

/**
 * @author Paul Ferraro
 */
public enum JavaTesterFactory implements MarshallingTesterFactory {
	INSTANCE;

	private final ByteBufferMarshaller marshaller = new JavaByteBufferMarshaller(ClassLoader.getSystemClassLoader(), null);

	@Override
	public <T> MarshallingTester<T> createTester() {
		return new MarshallingTester<>(new ByteBufferTestMarshaller<>(this.marshaller), List.of());
	}

	@Override
	public ByteBufferMarshaller getMarshaller() {
		return this.marshaller;
	}
}
