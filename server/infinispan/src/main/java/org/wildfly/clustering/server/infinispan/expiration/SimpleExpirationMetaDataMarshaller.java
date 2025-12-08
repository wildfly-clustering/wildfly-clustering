/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.expiration;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * ProtoStream marshaller for a {@link SimpleExpirationMetaData}.
 * @author Paul Ferraro
 */
public enum SimpleExpirationMetaDataMarshaller implements ProtoStreamMarshaller<SimpleExpirationMetaData> {
	/** Singleton instance */
	INSTANCE;

	private static final int MAX_IDLE_INDEX = 1;
	private static final int LAST_ACCESS_TIME_INDEX = 2;

	@Override
	public Class<? extends SimpleExpirationMetaData> getJavaClass() {
		return SimpleExpirationMetaData.class;
	}

	@Override
	public SimpleExpirationMetaData readFrom(ProtoStreamReader reader) throws IOException {
		Optional<Duration> maxIdle = Optional.empty();
		Optional<Instant> lastAccessTime = Optional.empty();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case MAX_IDLE_INDEX -> {
					maxIdle = Optional.of(reader.readObject(Duration.class));
				}
				case LAST_ACCESS_TIME_INDEX -> {
					lastAccessTime = Optional.of(reader.readObject(Instant.class));
				}
				default -> reader.skipField(tag);
			}
		}
		return new SimpleExpirationMetaData(maxIdle, lastAccessTime);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, SimpleExpirationMetaData metaData) throws IOException {
		writer.writeObject(MAX_IDLE_INDEX, metaData.getMaxIdle());
		writer.writeObject(LAST_ACCESS_TIME_INDEX, metaData.getLastAccessTime());
	}
}
