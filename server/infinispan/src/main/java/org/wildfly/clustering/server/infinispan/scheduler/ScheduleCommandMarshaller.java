/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.io.IOException;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * ProtoStream marshaller for a {@link ScheduleCommand}.
 * @author Paul Ferraro
 */
public enum ScheduleCommandMarshaller implements ProtoStreamMarshaller<ScheduleCommand<Object, Object>> {
	/** Singleton instance */
	INSTANCE;

	private static final byte KEY_INDEX = 1;
	private static final byte VALUE_INDEX = 2;

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ScheduleCommand<Object, Object>> getJavaClass() {
		return (Class<ScheduleCommand<Object, Object>>) (Class<?>) ScheduleCommand.class;
	}

	@Override
	public ScheduleCommand<Object, Object> readFrom(ProtoStreamReader reader) throws IOException {
		Object key = null;
		Object value = null;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case KEY_INDEX -> {
					key = reader.readAny();
				}
				case VALUE_INDEX -> {
					value = reader.readAny();
				}
				default -> reader.skipField(tag);
			}
		}
		return new ScheduleCommand<>(key, value);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, ScheduleCommand<Object, Object> command) throws IOException {
		Object key = command.getKey();
		if (key != null) {
			writer.writeAny(KEY_INDEX, key);
		}
		Object value = command.getValue();
		if (value != null) {
			writer.writeAny(VALUE_INDEX, value);
		}
	}
}
