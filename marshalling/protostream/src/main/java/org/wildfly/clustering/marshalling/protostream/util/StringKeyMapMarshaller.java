/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * An optimized marshaller for a {@link Map} whose keys are strings.
 * @author Paul Ferraro
 * @param <V> the map value type
 * @param <T> the map type
 */
public class StringKeyMapMarshaller<V, T extends Map<String, V>> implements ProtoStreamMarshaller<T> {
	private static final int ENTRY_INDEX = 1;

	private final Supplier<T> factory;
	private final Function<T, Set<String>> keys;
	private final BiFunction<T, String, V> value;
	private final BiConsumer<T, Map.Entry<String, V>> accumulator;

	/**
	 * Creates a marshaller for a string-keyed map.
	 * @param factory the map factory
	 * @param keys a function returning the keys of the map
	 * @param value a function returning the value for a given key
	 * @param accumulator an accumulator of read entries
	 */
	public StringKeyMapMarshaller(Supplier<T> factory, Function<T, Set<String>> keys, BiFunction<T, String, V> value, BiConsumer<T, Map.Entry<String, V>> accumulator) {
		this.factory = factory;
		this.keys = keys;
		this.value = value;
		this.accumulator = accumulator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends T> getJavaClass() {
		return (Class<T>) this.factory.get().getClass();
	}

	@Override
	public T readFrom(ProtoStreamReader reader) throws IOException {
		T map = this.factory.get();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			int index = WireType.getTagFieldNumber(tag);
			switch (index) {
				case ENTRY_INDEX -> {
					Map.Entry<String, V> entry = reader.readObject(StringKeyMapEntry.class);
					this.accumulator.accept(map, entry);
				}
				default -> reader.skipField(tag);
			}
		}
		return map;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, T map) throws IOException {
		for (String key : this.keys.apply(map)) {
			V value = this.value.apply(map, key);
			writer.writeObject(ENTRY_INDEX, new StringKeyMapEntry<>(key, value));
		}
	}
}
