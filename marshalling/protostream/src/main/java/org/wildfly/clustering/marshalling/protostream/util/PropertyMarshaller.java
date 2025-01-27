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
 * A marshaller for a property, i.e. a {@link java.util.Map.Entry} whose key and value are strings.
 * @author Paul Ferraro
 * @param <T> the map entry type
 */
public class PropertyMarshaller<T extends Map.Entry<String, String>> implements ProtoStreamMarshaller<T> {

	private static final int KEY_INDEX = 1;
	private static final int VALUE_INDEX = 2;

	private final BiFunction<String, String, T> factory;

	public PropertyMarshaller(BiFunction<String, String, T> factory) {
		this.factory = factory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends T> getJavaClass() {
		return (Class<T>) this.factory.apply(null, null).getClass();
	}

	@Override
	public T readFrom(ProtoStreamReader reader) throws IOException {
		String key = null;
		String value = null;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			int index = WireType.getTagFieldNumber(tag);
			switch (index) {
				case KEY_INDEX:
					key = reader.readString();
					break;
				case VALUE_INDEX:
					value = reader.readString();
					break;
				default:
					reader.skipField(tag);
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
		String value = entry.getValue();
		if (value != null) {
			writer.writeString(VALUE_INDEX, value);
		}
	}
}
