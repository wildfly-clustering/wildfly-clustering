/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.OptionalInt;
import java.util.UUID;

import org.wildfly.clustering.marshalling.Serializer;

/**
 * @author Paul Ferraro
 */
public enum UUIDSerializer implements Serializer<UUID> {
	INSTANCE;

	@Override
	public void write(DataOutput output, UUID id) throws IOException {
		output.writeLong(id.getMostSignificantBits());
		output.writeLong(id.getLeastSignificantBits());
	}

	@Override
	public UUID read(DataInput input) throws IOException {
		return new UUID(input.readLong(), input.readLong());
	}

	@Override
	public OptionalInt size(UUID object) {
		return OptionalInt.of(Long.BYTES * 2);
	}
}
