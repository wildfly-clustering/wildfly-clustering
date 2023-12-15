/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.common.function.Functions;

/**
 * Marshaller for a single scalar value.
 * This marshaller does not write any tags, nor does it read beyond a single value.
 * @author Paul Ferraro
 * @param <T> the type of this marshaller
 */
public interface ScalarMarshaller<T> extends Marshallable<T> {

	/**
	 * Returns the wire type of the scalar value written by this marshaller.
	 * @return the wire type of the scalar value written by this marshaller.
	 */
	WireType getWireType();

	default <V> ProtoStreamMarshaller<V> toMarshaller(Class<? extends V> targetClass, Function<V, T> function, Function<T, V> factory) {
		return this.wrap(targetClass, Functions.constantSupplier(null), value -> false, function, factory);
	}

	default <V> ProtoStreamMarshaller<V> toMarshaller(Class<? extends V> targetClass, Supplier<V> defaultFactory, Function<V, T> function, Function<T, V> factory) {
		return this.toMarshaller(targetClass, defaultFactory, Objects::equals, function, factory);
	}

	default <V> ProtoStreamMarshaller<V> toMarshaller(Class<? extends V> targetClass, Supplier<V> defaultFactory, BiPredicate<T, T> equals, Function<V, T> function, Function<T, V> factory) {
		return this.wrap(targetClass, defaultFactory, new Predicate<>() {
			@Override
			public boolean test(V value) {
				return equals.test(function.apply(value), function.apply(defaultFactory.get()));
			}
		}, function, factory);
	}

	default <V> ProtoStreamMarshaller<V> wrap(Class<? extends V> targetClass, Supplier<V> defaultFactory, Predicate<V> skipWrite, Function<V, T> function, Function<T, V> factory) {
		return new ProtoStreamMarshaller<>() {
			@Override
			public Class<? extends V> getJavaClass() {
				return targetClass;
			}

			@Override
			public V readFrom(ProtoStreamReader reader) throws IOException {
				V value = defaultFactory.get();
				while (!reader.isAtEnd()) {
					int tag = reader.readTag();
					switch (WireType.getTagFieldNumber(tag)) {
						case 1:
							value = factory.apply(ScalarMarshaller.this.readFrom(reader));
							break;
						default:
							reader.skipField(tag);
					}
				}
				return value;
			}

			@Override
			public void writeTo(ProtoStreamWriter writer, V value) throws IOException {
				if (!skipWrite.test(value)) {
					writer.writeTag(1, ScalarMarshaller.this.getWireType());
					ScalarMarshaller.this.writeTo(writer, function.apply(value));
				}
			}
		};
	}
}
