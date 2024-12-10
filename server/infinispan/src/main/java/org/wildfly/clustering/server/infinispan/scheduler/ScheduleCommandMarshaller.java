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
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public class ScheduleCommandMarshaller<I, M> implements ProtoStreamMarshaller<ScheduleCommand<I, M>> {

	private static final byte ID_INDEX = 1;
	private static final byte META_DATA_INDEX = 2;

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ScheduleCommand<I, M>> getJavaClass() {
		return (Class<ScheduleCommand<I, M>>) (Class<?>) ScheduleCommand.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ScheduleCommand<I, M> readFrom(ProtoStreamReader reader) throws IOException {
		I id = null;
		M metaData = null;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case ID_INDEX:
					id = (I) reader.readAny();
					break;
				case META_DATA_INDEX:
					metaData = (M) reader.readAny();
					break;
				default:
					reader.skipField(tag);
			}
		}
		return new ScheduleCommand<>(id, metaData);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, ScheduleCommand<I, M> command) throws IOException {
		I id = command.getId();
		if (id != null) {
			writer.writeAny(ID_INDEX, id);
		}
		M metaData = command.getMetaData();
		if (metaData != null) {
			writer.writeAny(META_DATA_INDEX, metaData);
		}
	}
}
