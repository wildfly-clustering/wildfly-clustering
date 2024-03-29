/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Supplier;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * A very generic marshaller for use with classes whose state is not publicly available for reading or writing except by pure reflection.
 * @param <T> the target type of this marshaller
 * @author Paul Ferraro
 */
public class FieldMarshaller<T> implements ProtoStreamMarshaller<T> {

	private final Class<? extends T> type;
	private final Supplier<? extends T> factory;
	private final Field[] fields;

	public FieldMarshaller(Class<? extends T> type, Class<?>... memberTypes) {
		this(type, defaultFactory(type), memberTypes);
	}

	private static <T> Supplier<T> defaultFactory(Class<T> type) {
		Constructor<T> constructor = Reflect.getConstructor(type);
		return () -> Reflect.newInstance(constructor);
	}

	public FieldMarshaller(Class<? extends T> type, Supplier<? extends T> factory, Class<?>... memberTypes) {
		this.type = type;
		this.factory = factory;
		this.fields = new Field[memberTypes.length];
		for (int i = 0; i < this.fields.length; ++i) {
			this.fields[i] = Reflect.findField(type, memberTypes[i]);
		}
	}

	@Override
	public Class<? extends T> getJavaClass() {
		return this.type;
	}

	@Override
	public T readFrom(ProtoStreamReader reader) throws IOException {
		T result = this.factory.get();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			int index = WireType.getTagFieldNumber(tag);
			if ((index > 0) || (index <= this.fields.length)) {
				Reflect.setValue(result, this.fields[index - 1], reader.readAny());
			} else {
				reader.skipField(tag);
			}
		}
		return result;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, T source) throws IOException {
		for (int i = 0; i < this.fields.length; ++i) {
			Object value = Reflect.getValue(source, this.fields[i]);
			if (value != null) {
				writer.writeAny(i + 1, value);
			}
		}
	}
}
