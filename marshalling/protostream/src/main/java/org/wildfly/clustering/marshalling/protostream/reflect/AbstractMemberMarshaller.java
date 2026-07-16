/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Generic marshaller based on non-public members.
 * @param <T> the target type of this marshaller
 * @author Paul Ferraro
 */
public abstract class AbstractMemberMarshaller<T> implements ProtoStreamMarshaller<T>, Function<Object[], T> {
	private final Class<? extends T> type;
	private final List<Function<T, Object>> members;

	/**
	 * Creates a marshaller using the specified member fields.
	 * @param type the marshalled object type
	 * @param handleFactory a member handle factory
	 * @param memberTypes the field types
	 */
	public AbstractMemberMarshaller(Class<T> type, BiFunction<Class<T>, Class<?>, Function<T, Object>> handleFactory, Class<?>... memberTypes) {
		this.type = type;
		this.members = new ArrayList<>(memberTypes.length);
		for (Class<?> memberType : memberTypes) {
			this.members.add(handleFactory.apply(type, memberType));
		}
	}

	static <T, R> R invoke(MethodHandle handle, T parameter) {
		try {
			return (R) handle.invoke(parameter);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	static <T, R> R read(VarHandle handle, T parameter) {
		return (R) handle.get(parameter);
	}

	@Override
	public Class<? extends T> getJavaClass() {
		return this.type;
	}

	@Override
	public T readFrom(ProtoStreamReader reader) throws IOException {
		Object[] values = new Object[this.members.size()];
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			int index = WireType.getTagFieldNumber(tag);
			if ((index > 0) || (index <= values.length)) {
				values[index - 1] = reader.readAny();
			} else {
				reader.skipField(tag);
			}
		}
		return this.apply(values);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, T source) throws IOException {
		for (int i = 0; i < this.members.size(); ++i) {
			Object value = this.members.get(i).apply(source);
			if (value != null) {
				writer.writeAny(i + 1, value);
			}
		}
	}
}
