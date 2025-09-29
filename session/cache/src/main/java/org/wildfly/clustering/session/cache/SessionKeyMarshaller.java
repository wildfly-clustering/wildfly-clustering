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
import org.wildfly.clustering.session.IdentifierMarshallerProvider;

/**
 * Scalar marshaller for a session identifier.
 * @author Paul Ferraro
 * @param <K> the cache key type
 */
public class SessionKeyMarshaller<K extends Key<String>> implements ProtoStreamMarshaller<K> {
	private static final int IDENTIFIER_INDEX = 1;
	@SuppressWarnings("removal")
	private static final Marshaller<String, ByteBuffer> IDENTIFIER_MARSHALLER = AccessController.doPrivileged(new PrivilegedAction<Optional<Marshaller<String, ByteBuffer>>>() {
		@Override
		public Optional<Marshaller<String, ByteBuffer>> run() {
			return ServiceLoader.load(IdentifierMarshallerProvider.class, IdentifierMarshallerProvider.class.getClassLoader()).findFirst().map(IdentifierMarshallerProvider::getMarshaller);
		}
	}).orElseThrow(IllegalStateException::new);

	private final Function<String, K> factory;

	public SessionKeyMarshaller(Function<String, K> factory) {
		this.factory = factory;
	}

	@Override
	public K readFrom(ProtoStreamReader reader) throws IOException {
		ByteBuffer buffer = null;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case IDENTIFIER_INDEX -> {
					buffer = reader.readByteBuffer();
				}
				default -> reader.skipField(tag);
			}
		}
		return this.factory.apply(IDENTIFIER_MARSHALLER.read(buffer));
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, K key) throws IOException {
		String id = key.getId();
		if (id != null) {
			writer.writeBytes(IDENTIFIER_INDEX, IDENTIFIER_MARSHALLER.write(id));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends K> getJavaClass() {
		return (Class<K>) this.factory.apply("").getClass();
	}
}
