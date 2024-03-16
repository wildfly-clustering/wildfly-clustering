/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ServiceLoader;
import java.util.function.Function;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;
import org.wildfly.clustering.marshalling.protostream.ScalarMarshaller;
import org.wildfly.clustering.session.IdentifierMarshallerProvider;

/**
 * Scalar marshaller for a session identifier.
 * @author Paul Ferraro
 */
public enum IdentifierMarshaller implements ScalarMarshaller<String> {
	INSTANCE;

	private final Marshaller<String, ByteBuffer> marshaller = ServiceLoader.load(IdentifierMarshallerProvider.class, IdentifierMarshallerProvider.class.getClassLoader()).findFirst().map(IdentifierMarshallerProvider::getMarshaller).orElseThrow(IllegalStateException::new);

	@Override
	public String readFrom(ProtoStreamReader reader) throws IOException {
		return this.marshaller.read(reader.readByteBuffer());
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, String id) throws IOException {
		ByteBuffer buffer = this.marshaller.write(id);
		int offset = buffer.arrayOffset();
		int length = buffer.limit() - offset;
		writer.writeVarint32(length);
		writer.writeRawBytes(buffer.array(), offset, length);
	}

	@Override
	public Class<? extends String> getJavaClass() {
		return String.class;
	}

	@Override
	public WireType getWireType() {
		return WireType.LENGTH_DELIMITED;
	}

	@SuppressWarnings("unchecked")
	public static <K extends Key<String>> ProtoStreamMarshaller<K> getKeyMarshaller(Function<String, K> factory) {
		return INSTANCE.toMarshaller((Class<K>) factory.apply("").getClass(), Key::getId, factory);
	}
}
