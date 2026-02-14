/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.util.UUID;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.FieldSetMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshaller for the fields of a {@link UUID} using fixed size longs.
 * @author Paul Ferraro
 */
public enum UUIDMarshaller implements FieldSetMarshaller.Simple<UUID> {
	/** Singleton instance */
	INSTANCE;

	private static final UUID DEFAULT_VALUE = new UUID(0L, 0L);

	private static final int MOST_SIGNIFICANT_BITS_INDEX = 0;
	private static final int LEAST_SIGNIFICANT_BITS_INDEX = 1;
	private static final int FIELDS = 2;

	@Override
	public UUID createInitialValue() {
		return DEFAULT_VALUE;
	}

	@Override
	public int getFields() {
		return FIELDS;
	}

	@Override
	public UUID readFrom(ProtoStreamReader reader, int index, WireType type, UUID id) throws IOException {
		return switch (index) {
			case MOST_SIGNIFICANT_BITS_INDEX -> new UUID(reader.readSFixed64(), id.getLeastSignificantBits());
			case LEAST_SIGNIFICANT_BITS_INDEX -> new UUID(id.getMostSignificantBits(), reader.readSFixed64());
			default -> reader.skipField(type, id);
		};
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, UUID uuid) throws IOException {
		long mostSignificantBits = uuid.getMostSignificantBits();
		if (mostSignificantBits != DEFAULT_VALUE.getMostSignificantBits()) {
			writer.writeSFixed64(MOST_SIGNIFICANT_BITS_INDEX, mostSignificantBits);
		}
		long leastSignificantBits = uuid.getLeastSignificantBits();
		if (leastSignificantBits != DEFAULT_VALUE.getLeastSignificantBits()) {
			writer.writeSFixed64(LEAST_SIGNIFICANT_BITS_INDEX, leastSignificantBits);
		}
	}
}
