/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshaller for a {@link LinkedHashMap}.
 * @author Paul Ferraro
 */
public class LinkedHashMapMarshaller extends AbstractMapMarshaller<LinkedHashMap<Object, Object>> {

	private static final int ACCESS_ORDER_INDEX = VALUE_INDEX + 1;

	private  static final Function<LinkedHashMap<Object, Object>, Boolean> ACCESS_ORDER = new Function<>() {
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
		super((Class<LinkedHashMap<Object, Object>>) (Class<?>) LinkedHashMap.class);
	}

	@Override
	public LinkedHashMap<Object, Object> readFrom(ProtoStreamReader reader) throws IOException {
		LinkedHashMap<Object, Object> map = new LinkedHashMap<>(16, 0.75f, false);
		List<Object> keys = new LinkedList<>();
		List<Object> values = new LinkedList<>();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			int index = WireType.getTagFieldNumber(tag);
			switch (index) {
				case KEY_INDEX:
					keys.add(reader.readAny());
					break;
				case VALUE_INDEX:
					values.add(reader.readAny());
					break;
				case ACCESS_ORDER_INDEX:
					map = new LinkedHashMap<>(16, 0.75f, reader.readBool());
					break;
				default:
					reader.skipField(tag);
			}
		}
		Iterator<Object> keyIterator = keys.iterator();
		Iterator<Object> valueIterator = values.iterator();
		while (keyIterator.hasNext() || valueIterator.hasNext()) {
			map.put(keyIterator.next(), valueIterator.next());
		}
		return map;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, LinkedHashMap<Object, Object> map) throws IOException {
		synchronized (map) { // Avoid ConcurrentModificationException
			super.writeTo(writer, map);
			boolean accessOrder = ACCESS_ORDER.apply(map);
			if (accessOrder) {
				writer.writeBool(ACCESS_ORDER_INDEX, accessOrder);
			}
		}
	}
}
