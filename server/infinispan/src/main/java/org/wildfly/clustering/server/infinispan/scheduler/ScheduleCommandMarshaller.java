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

	private static final byte ID_INDEX = 1;
	private static final byte META_DATA_INDEX = 2;

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ScheduleCommand<Object, Object>> getJavaClass() {
		return (Class<ScheduleCommand<Object, Object>>) (Class<?>) ScheduleCommand.class;
	}

	@Override
	public ScheduleCommand<Object, Object> readFrom(ProtoStreamReader reader) throws IOException {
		Object id = null;
		Object metaData = null;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case ID_INDEX -> {
					id = reader.readAny();
				}
				case META_DATA_INDEX -> {
					metaData = reader.readAny();
				}
				default -> reader.skipField(tag);
			}
		}
		return new ScheduleCommand<>(id, metaData);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, ScheduleCommand<Object, Object> command) throws IOException {
		Object id = command.getId();
		if (id != null) {
			writer.writeAny(ID_INDEX, id);
		}
		Object metaData = command.getMetaData();
		if (metaData != null) {
			writer.writeAny(META_DATA_INDEX, metaData);
		}
	}
}
