/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.util.OptionalInt;

import org.infinispan.protostream.ProtobufTagMarshaller;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;

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

	/**
	 * Returns a new marshaller instance for a decorator of the type handled by this marshaller, created from the specified factory.
	 * @param <V> the decorator type
	 * @param type the decorator instance type of the new marshaller
	 * @param wrapper a function for creating the decorator from the value read by this marshaller.\
	 * @return a new marshaller
	 */
	default <V extends T> ProtoStreamMarshaller<V> wrap(Class<? extends V> type, java.util.function.Function<T, V> wrapper) {
		return wrap(type, Function.identity(), wrapper);
	}

	/**
	 * Returns a new marshaller instance for a wrapper, using the specified wrapper and unwrapper functions.
	 * @param <V> the wrapper type
	 * @param type the target class of the new marshaller
	 * @param unwrapper a function exposing the value of this marshalller from its wrapper
	 * @param wrapper a function creating the wrapped instance from the value read by this marshaller.
	 * @return a new marshaller
	 */
	default <V> ProtoStreamMarshaller<V> wrap(Class<? extends V> type, java.util.function.Function<V, T> unwrapper, java.util.function.Function<T, V> wrapper) {
		ProtoStreamMarshaller<T> marshaller = this;
		return new ProtoStreamMarshaller<>() {
			@Override
			public Class<? extends V> getJavaClass() {
				return type;
			}

			@Override
			public V readFrom(ProtoStreamReader reader) throws IOException {
				return wrapper.apply(marshaller.readFrom(reader));
			}

			@Override
			public void writeTo(ProtoStreamWriter writer, V value) throws IOException {
				marshaller.writeTo(writer, unwrapper.apply(value));
			}

			@Override
			public OptionalInt size(ProtoStreamSizeOperation operation, V value) {
				return marshaller.size(operation, unwrapper.apply(value));
			}
		};
	}

	/**
	 * Creates a trivial marshaller for a constant value.
	 * @param <T> the marshaller type
	 * @param value a constant value
	 * @return a new marshaller
	 */
	static <T> ProtoStreamMarshaller<T> of(T value) {
		return of(Supplier.of(value));
	}

	/**
	 * Creates a trivial marshaller for a constant value.
	 * @param <T> the marshaller type
	 * @param factory a supplier of the constant value
	 * @return a new marshaller
	 */
	static <T> ProtoStreamMarshaller<T> of(java.util.function.Supplier<T> factory) {
		return new ProtoStreamMarshaller<>() {
			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends T> getJavaClass() {
				return (Class<T>) factory.get().getClass();
			}

			@Override
			public T readFrom(ProtoStreamReader reader) {
				return factory.get();
			}

			@Override
			public void writeTo(ProtoStreamWriter writer, T value) {
				// Nothing to write
			}

			@Override
			public OptionalInt size(ProtoStreamSizeOperation operation, T value) {
				return OptionalInt.of(0);
			}
		};
	}

	/**
	 * Creates a marshaller for an enum.
	 * @param <E> the marshaller type
	 * @param enumClass the enum type
	 * @return a new marshaller
	 */
	static <E extends Enum<E>> ProtoStreamMarshaller<E> of(Class<E> enumClass) {
		return new EnumMarshaller<>(enumClass);
	}
}
