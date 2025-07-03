/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.Any;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshaller for an {@link EnumMap}.
 * @author Paul Ferraro
 * @param <E> the enum key type of this marshaller
 */
public class EnumMapMarshaller<E extends Enum<E>> implements ProtoStreamMarshaller<EnumMap<E, Object>> {

	static final Field ENUM_MAP_KEY_CLASS_FIELD = Reflect.findField(EnumMap.class, Class.class);

	private static final int CLASS_INDEX = 1;
	private static final int ELEMENT_INDEX = 2;

	@Override
	public EnumMap<E, Object> readFrom(ProtoStreamReader reader) throws IOException {
		Class<E> enumClass = null;
		List<Object> elements = new LinkedList<>();

		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case CLASS_INDEX -> {
					enumClass = reader.readObject(Class.class);
				}
				case ELEMENT_INDEX -> {
					elements.add(reader.readAny());
				}
				default -> reader.skipField(tag);
			}
		}
		EnumMap<E, Object> map = new EnumMap<>(enumClass);
		E[] values = enumClass.getEnumConstants();
		ListIterator<Object> iterator = elements.listIterator();
		while (iterator.hasNext()) {
			int index = iterator.nextIndex();
			Object element = iterator.next();
			if (element != null) {
				map.put(values[index], (element == Any.NULL) ? null : element);
			}
		}
		return map;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, EnumMap<E, Object> map) throws IOException {
		Class<E> enumClass = this.findEnumClass(map);
		writer.writeObject(CLASS_INDEX, enumClass);
		for (E key : EnumSet.allOf(enumClass)) {
			if (map.containsKey(key)) {
				Object value = map.get(key);
				// Use a NULL marker to distinguish between no mapping and null mapping
				writer.writeAny(ELEMENT_INDEX, (value == null) ? Any.NULL : value);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends EnumMap<E, Object>> getJavaClass() {
		return (Class<? extends EnumMap<E, Object>>) (Class<?>) EnumMap.class;
	}

	private Class<E> findEnumClass(EnumMap<E, Object> map) {
		Iterator<E> values = map.keySet().iterator();
		if (values.hasNext()) {
			return values.next().getDeclaringClass();
		}
		// If EnumMap is empty, we need to resort to reflection to obtain the enum type
		return Reflect.getValue(map, ENUM_MAP_KEY_CLASS_FIELD, Class.class);
	}
}
