/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.util.List;
import java.util.function.Supplier;

import org.infinispan.protostream.ImmutableSerializationContext;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.ByteBufferTestMarshaller;
import org.wildfly.clustering.marshalling.MarshallingTester;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.java.JavaTesterFactory;

/**
 * @author Paul Ferraro
 */
public class ProtoStreamTesterFactory implements MarshallingTesterFactory, Supplier<ByteBufferMarshaller> {

	private final ByteBufferMarshaller marshaller;

	public ProtoStreamTesterFactory() {
		this(List.of());
	}

	public ProtoStreamTesterFactory(Iterable<SerializationContextInitializer> initializers) {
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		ImmutableSerializationContext context = SerializationContextBuilder.newInstance(ClassLoaderMarshaller.of(loader)).load(loader).register(initializers).build();
		this.marshaller = new ProtoStreamByteBufferMarshaller(context);
	}

	@Override
	public <T> MarshallingTester<T> createTester() {
		return new MarshallingTester<>(new ByteBufferTestMarshaller<>(this.marshaller), List.of(JavaTesterFactory.INSTANCE.get()));
	}

	@Override
	public ByteBufferMarshaller get() {
		return this.marshaller;
	}
}
