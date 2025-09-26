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
 * An optimized marshaller for a {@link java.util.Map.Entry} whose key is a string.
 * @author Paul Ferraro
 * @param <T> the map entry type of this marshaller
 */
public class StringKeyMapEntryMarshaller<T extends Map.Entry<String, Object>> implements ProtoStreamMarshaller<T> {

	private static final int KEY_INDEX = 1;
	private static final int VALUE_INDEX = 2;

	private final Class<? extends T> targetClass;
	private final BiFunction<String, Object, T> factory;

	/**
	 * Creates a marshaller for a string-keyed map entry
	 * @param factory the map entry factory
	 */
	@SuppressWarnings("unchecked")
	public StringKeyMapEntryMarshaller(BiFunction<String, Object, T> factory) {
		this.targetClass = (Class<T>) factory.apply("", null).getClass();
		this.factory = factory;
	}

	@Override
	public T readFrom(ProtoStreamReader reader) throws IOException {
		String key = null;
		Object value = null;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			int index = WireType.getTagFieldNumber(tag);
			switch (index) {
				case KEY_INDEX -> {
					key = reader.readString();
				}
				case VALUE_INDEX -> {
					value = reader.readAny();
				}
				default -> reader.skipField(tag);
			}
		}
		return this.factory.apply(key, value);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, T entry) throws IOException {
		String key = entry.getKey();
		if (key != null) {
			writer.writeString(KEY_INDEX, key);
		}
		Object value = entry.getValue();
		if (value != null) {
			writer.writeAny(VALUE_INDEX, value);
		}
	}

	@Override
	public Class<? extends T> getJavaClass() {
		return this.targetClass;
	}
}
