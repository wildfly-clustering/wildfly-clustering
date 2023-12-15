/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.infinispan.protostream.ProtobufTagMarshaller;
import org.wildfly.common.function.Functions;

/**
 * A {@link ProtobufTagMarshaller} that include a facility for computing buffer sizes.
 * @author Paul Ferraro
 * @param <T> the type of this marshaller.
 */
public interface ProtoStreamMarshaller<T> extends ProtobufTagMarshaller<T>, Marshallable<T> {

	@Override
	default String getTypeName() {
		Class<?> targetClass = this.getJavaClass();
		Package targetPackage = targetClass.getPackage();
		return (targetPackage != null) ? (targetPackage.getName() + '.' + targetClass.getSimpleName()) : targetClass.getSimpleName();
	}

	@Override
	default T read(ReadContext context) throws IOException {
		return this.readFrom(new DefaultProtoStreamReader(context));
	}

	@Override
	default void write(WriteContext context, T value) throws IOException {
		this.writeTo(new DefaultProtoStreamWriter(context), value);
	}

	default <V extends T> ProtoStreamMarshaller<V> map(Class<V> targetClass, Function<T, V> factory) {
		return map(targetClass, Functions.cast(Function.identity()), factory);
	}

	default <V> ProtoStreamMarshaller<V> map(Class<V> targetClass, Function<V, T> function, Function<T, V> factory) {
		return new ProtoStreamMarshaller<>() {
			@Override
			public Class<? extends V> getJavaClass() {
				return targetClass;
			}

			@Override
			public V readFrom(ProtoStreamReader reader) throws IOException {
				return factory.apply(ProtoStreamMarshaller.this.readFrom(reader));
			}

			@Override
			public void writeTo(ProtoStreamWriter writer, V value) throws IOException {
				ProtoStreamMarshaller.this.writeTo(writer, function.apply(value));
			}
		};
	}

	static <T> ProtoStreamMarshaller<T> of(T value) {
		return of(Functions.constantSupplier(value));
	}

	static <T> ProtoStreamMarshaller<T> of(Supplier<T> factory) {
		return new ProtoStreamMarshaller<>() {
			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends T> getJavaClass() {
				return (Class<T>) factory.get().getClass();
			}

			@Override
			public T readFrom(ProtoStreamReader reader) throws IOException {
				return factory.get();
			}

			@Override
			public void writeTo(ProtoStreamWriter writer, T value) throws IOException {
				// Nothing to write
			}
		};
	}

	static <E extends Enum<E>> ProtoStreamMarshaller<E> of(Class<E> enumClass) {
		return new EnumMarshaller<>(enumClass);
	}
}
