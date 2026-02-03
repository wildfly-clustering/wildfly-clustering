/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
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
	private final VarHandle[] handles;

	/**
	 * Creates a marshaller for the specified fields of the specified class.
	 * @param type a marshalled object type
	 * @param memberTypes the member types
	 */
	public FieldMarshaller(Class<? extends T> type, Class<?>... memberTypes) {
		this(type, defaultFactory(type), memberTypes);
	}

	private static <T> Supplier<T> defaultFactory(Class<T> type) {
		MethodHandle handle = Reflect.getConstructorHandle(type);
		return new Supplier<>() {
			@Override
			public T get() {
				try {
					return (T) handle.invokeExact();
				} catch (RuntimeException | Error e) {
					throw e;
				} catch (Throwable e) {
					throw new IllegalStateException(e);
				}
			}
		};
	}

	/**
	 * Creates a field marshaller for the specified fields of the specified class using the specified factory
	 * @param type a marshalled object type
	 * @param factory a factory for creating the field
	 * @param memberTypes the member types
	 */
	public FieldMarshaller(Class<? extends T> type, Supplier<? extends T> factory, Class<?>... memberTypes) {
		this.type = type;
		this.factory = factory;
		this.handles = new VarHandle[memberTypes.length];
		for (int i = 0; i < this.handles.length; ++i) {
			this.handles[i] = Reflect.findVarHandle(type, memberTypes[i]);
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
			if ((index > 0) || (index <= this.handles.length)) {
				VarHandle handle = this.handles[index - 1];
				handle.set(result, reader.readAny());
			} else {
				reader.skipField(tag);
			}
		}
		return result;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, T source) throws IOException {
		for (int i = 0; i < this.handles.length; ++i) {
			Object value = this.handles[i].get(source);
			if (value != null) {
				writer.writeAny(i + 1, value);
			}
		}
	}
}
