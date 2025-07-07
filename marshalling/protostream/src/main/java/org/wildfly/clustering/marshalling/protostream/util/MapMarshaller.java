/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Supplier;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;

/**
 * Marshaller for a {@link Map}.
 * @author Paul Ferraro
 * @param <K> the map key type
 * @param <V> the map value type
 * @param <T> the map type of this marshaller
 */
public class MapMarshaller<K, V, T extends Map<K, V>> extends AbstractMapMarshaller<K, V, T> {

	private final Supplier<T> factory;

	@SuppressWarnings("unchecked")
	public MapMarshaller(Supplier<T> factory) {
		super((Class<T>) factory.get().getClass());
		this.factory = factory;
	}

	@Override
	public T readFrom(ProtoStreamReader reader) throws IOException {
		T map = this.factory.get();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			int index = WireType.getTagFieldNumber(tag);
			switch (index) {
				case ENTRY_INDEX -> {
					Map.Entry<K, V> entry = reader.readObject(AbstractMap.SimpleEntry.class);
					map.put(entry.getKey(), entry.getValue());
				}
				default -> reader.skipField(tag);
			}
		}
		return map;
	}
}
