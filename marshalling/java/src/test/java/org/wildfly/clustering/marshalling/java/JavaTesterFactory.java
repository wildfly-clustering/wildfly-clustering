/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import java.util.List;
import java.util.function.Supplier;

import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.ByteBufferTestMarshaller;
import org.wildfly.clustering.marshalling.MarshallingTester;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;

/**
 * @author Paul Ferraro
 */
public enum JavaTesterFactory implements MarshallingTesterFactory, Supplier<ByteBufferMarshaller> {
	INSTANCE;

	private final ByteBufferMarshaller marshaller = new JavaByteBufferMarshaller();

	@Override
	public <T> MarshallingTester<T> createTester() {
		return new MarshallingTester<>(new ByteBufferTestMarshaller<>(this.marshaller), List.of());
	}

	@Override
	public ByteBufferMarshaller get() {
		return this.marshaller;
	}
}
