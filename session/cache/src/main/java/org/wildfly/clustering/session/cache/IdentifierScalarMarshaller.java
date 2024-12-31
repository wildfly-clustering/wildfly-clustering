/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.ScalarMarshaller;
import org.wildfly.clustering.session.IdentifierMarshallerProvider;

/**
 * Scalar marshaller for a session identifier.
 * @author Paul Ferraro
 */
public enum IdentifierScalarMarshaller implements ScalarMarshaller<String> {
	INSTANCE;

	@SuppressWarnings("removal")
	private final Marshaller<String, ByteBuffer> marshaller = AccessController.doPrivileged(new PrivilegedAction<Optional<Marshaller<String, ByteBuffer>>>() {
		@Override
		public Optional<Marshaller<String, ByteBuffer>> run() {
			return ServiceLoader.load(IdentifierMarshallerProvider.class, IdentifierMarshallerProvider.class.getClassLoader()).findFirst().map(IdentifierMarshallerProvider::getMarshaller);
		}
	}).orElseThrow(IllegalStateException::new);
	private final ScalarMarshaller<ByteBuffer> bufferMarshaller = Scalar.BYTE_BUFFER.cast(ByteBuffer.class);

	@Override
	public String readFrom(ProtoStreamReader reader) throws IOException {
		ByteBuffer buffer = this.bufferMarshaller.readFrom(reader);
		return this.marshaller.read(buffer);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, String id) throws IOException {
		ByteBuffer buffer = this.marshaller.write(id);
		this.bufferMarshaller.writeTo(writer, buffer);
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
	public <K extends Key<String>> ProtoStreamMarshaller<K> toKeyMarshaller(Function<String, K> factory) {
		return this.toMarshaller((Class<K>) factory.apply("").getClass(), Key::getId, factory);
	}
}
