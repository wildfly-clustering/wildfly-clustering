/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshaller for a {@link java.util.Map.Entry}
 * @author Paul Ferraro
 * @param <K> the map entry key type
 * @param <V> the map entry value type
 * @param <T> the map entry type of this marshaller
 */
public class MapEntryMarshaller<K, V, T extends Map.Entry<K, V>> implements ProtoStreamMarshaller<T> {

	private static final int KEY_INDEX = 1;
	private static final int VALUE_INDEX = 2;

	private final Class<? extends T> targetClass;
	private final BiFunction<K, V, T> factory;

	@SuppressWarnings("unchecked")
	public MapEntryMarshaller(BiFunction<K, V, T> factory) {
		this.targetClass = (Class<T>) factory.apply(null, null).getClass();
		this.factory = factory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T readFrom(ProtoStreamReader reader) throws IOException {
		K key = null;
		V value = null;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			int index = WireType.getTagFieldNumber(tag);
			switch (index) {
				case KEY_INDEX -> {
					key = (K) reader.readAny();
				}
				case VALUE_INDEX -> {
					value = (V) reader.readAny();
				}
				default -> reader.skipField(tag);
			}
		}
		return this.factory.apply(key, value);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, T entry) throws IOException {
		K key = entry.getKey();
		if (key != null) {
			writer.writeAny(KEY_INDEX, key);
		}
		V value = entry.getValue();
		if (value != null) {
			writer.writeAny(VALUE_INDEX, value);
		}
	}

	@Override
	public Class<? extends T> getJavaClass() {
		return this.targetClass;
	}
}
