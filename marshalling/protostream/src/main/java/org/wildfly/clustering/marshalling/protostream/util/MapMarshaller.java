/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.function.IntFunction;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;

/**
 * Marshaller for a {@link Map}.
 * @author Paul Ferraro
 * @param <K> the map key type
 * @param <V> the map value type
 * @param <T> the map type of this marshaller
 */
public class MapMarshaller<K, V, T extends Map<K, V>> extends AbstractMapMarshaller<K, V, T> {

	private final IntFunction<T> factory;

	/**
	 * Creates a marshaller for a map.
	 * @param factory a map factory
	 */
	@SuppressWarnings("unchecked")
	public MapMarshaller(IntFunction<T> factory) {
		super((Class<T>) factory.apply(0).getClass());
		this.factory = factory;
	}

	@Override
	public T readFrom(ProtoStreamReader reader) throws IOException {
		List<Map.Entry<K, V>> entries = new LinkedList<>();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case ENTRY_INDEX -> entries.add(reader.readObject(AbstractMap.SimpleEntry.class));
				default -> reader.skipField(tag);
			}
		}
		T map = this.factory.apply(entries.size());
		for (Map.Entry<K, V> entry : entries) {
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}
}
