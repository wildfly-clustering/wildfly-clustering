/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshaller for a {@link LinkedHashMap}.
 * @author Paul Ferraro
 * @param <K> the map key type
 * @param <V> the map value type
 */
public class LinkedHashMapMarshaller<K, V> extends AbstractMapMarshaller<K, V, LinkedHashMap<K, V>> {
	static final ProtoStreamMarshaller<?> INSTANCE = new LinkedHashMapMarshaller<>();

	private static final int ACCESS_ORDER_INDEX = ENTRY_INDEX + 1;

	private static final VarHandle ACCESS_ORDER_HANDLE = Reflect.getVarHandle(LinkedHashMap.class, boolean.class);

	@SuppressWarnings("unchecked")
	private LinkedHashMapMarshaller() {
		super((Class<LinkedHashMap<K, V>>) (Class<?>) LinkedHashMap.class);
	}

	@Override
	public LinkedHashMap<K, V> readFrom(ProtoStreamReader reader) throws IOException {
		boolean accessOrder = false;
		List<Map.Entry<K, V>> entries = new LinkedList<>();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case ENTRY_INDEX -> entries.add(reader.readObject(AbstractMap.SimpleEntry.class));
				case ACCESS_ORDER_INDEX -> accessOrder = reader.readBool();
				default -> reader.skipField(tag);
			}
		}
		LinkedHashMap<K, V> map = new LinkedHashMap<>(entries.size(), 0.75f, accessOrder);
		for (Map.Entry<K, V> entry : entries) {
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, LinkedHashMap<K, V> map) throws IOException {
		synchronized (map) { // Avoid ConcurrentModificationException
			super.writeTo(writer, map);
			boolean accessOrder = (Boolean) ACCESS_ORDER_HANDLE.get(map);
			if (accessOrder) {
				writer.writeBool(ACCESS_ORDER_INDEX, accessOrder);
			}
		}
	}
}
