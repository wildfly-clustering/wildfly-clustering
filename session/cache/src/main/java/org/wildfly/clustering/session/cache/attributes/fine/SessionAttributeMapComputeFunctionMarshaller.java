/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.fine;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * @author Paul Ferraro
 */
public class SessionAttributeMapComputeFunctionMarshaller implements ProtoStreamMarshaller<SessionAttributeMapComputeFunction<Object>> {
	private static final int ENTRY_INDEX = 1;

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends SessionAttributeMapComputeFunction<Object>> getJavaClass() {
		return (Class<SessionAttributeMapComputeFunction<Object>>) (Class<?>) SessionAttributeMapComputeFunction.class;
	}

	@Override
	public SessionAttributeMapComputeFunction<Object> readFrom(ProtoStreamReader reader) throws IOException {
		Map<String, Object> map = new TreeMap<>();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case ENTRY_INDEX -> {
					Map.Entry<String, Object> entry = reader.readObject(SessionAttributeMapEntry.class);
					map.put(entry.getKey(), entry.getValue());
				}
				default -> reader.skipField(tag);
			}
		}
		return new SessionAttributeMapComputeFunction<>(map);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, SessionAttributeMapComputeFunction<Object> function) throws IOException {
		for (Map.Entry<String, Object> entry : function.getOperand().entrySet()) {
			writer.writeObject(ENTRY_INDEX, new SessionAttributeMapEntry<>(entry));
		}
	}
}
