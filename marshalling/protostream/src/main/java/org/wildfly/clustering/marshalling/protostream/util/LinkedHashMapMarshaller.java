/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshaller for a {@link LinkedHashMap}.
 * @author Paul Ferraro
 * @param <K> the map key type
 * @param <V> the map value type
 */
public class LinkedHashMapMarshaller<K, V> extends AbstractMapMarshaller<K, V, LinkedHashMap<K, V>> {

	private static final int ACCESS_ORDER_INDEX = ENTRY_INDEX + 1;

	private static final Function<LinkedHashMap<Object, Object>, Boolean> ACCESS_ORDER = new Function<>() {
		@Override
		public Boolean apply(LinkedHashMap<Object, Object> map) {
			Object insertOrder = new Object();
			map.put(insertOrder, null);
			try {
				Object accessOrder = new Object();
				map.put(accessOrder, null);
				try {
					// Access first inserted entry
					// If map uses access order, this element will move to the tail of the map
					map.get(insertOrder);
					Iterator<Object> keys = map.keySet().iterator();
					Object element = keys.next();
					while ((element != insertOrder) && (element != accessOrder)) {
						element = keys.next();
					}
					return element == accessOrder;
				} finally {
					map.remove(accessOrder);
				}
			} finally {
				map.remove(insertOrder);
			}
		}
	};

	@SuppressWarnings("unchecked")
	public LinkedHashMapMarshaller() {
		super((Class<LinkedHashMap<K, V>>) (Class<?>) LinkedHashMap.class);
	}

	@Override
	public LinkedHashMap<K, V> readFrom(ProtoStreamReader reader) throws IOException {
		boolean accessOrder = false;
		List<Map.Entry<K, V>> entries = new LinkedList<>();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			int index = WireType.getTagFieldNumber(tag);
			switch (index) {
				case ENTRY_INDEX -> {
					entries.add(reader.readObject(AbstractMap.SimpleEntry.class));
				}
				case ACCESS_ORDER_INDEX -> {
					accessOrder = reader.readBool();
				}
				default -> reader.skipField(tag);
			}
		}
		LinkedHashMap<K, V> map = new LinkedHashMap<>(16, 0.75f, accessOrder);
		for (Map.Entry<K, V> entry : entries) {
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, LinkedHashMap<K, V> map) throws IOException {
		synchronized (map) { // Avoid ConcurrentModificationException
			super.writeTo(writer, map);
			@SuppressWarnings("unchecked")
			boolean accessOrder = ACCESS_ORDER.apply((LinkedHashMap<Object, Object>) map);
			if (accessOrder) {
				writer.writeBool(ACCESS_ORDER_INDEX, accessOrder);
			}
		}
	}
}
