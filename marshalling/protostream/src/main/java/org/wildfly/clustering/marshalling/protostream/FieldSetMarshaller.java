/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Supplier;

import org.infinispan.protostream.descriptors.WireType;

/**
 * Marshaller for a set of fields, to be shared between multiple marshallers.
 * @author Paul Ferraro
 * @param <T> the writer type
 * @param <V> the reader type
 */
public interface FieldSetMarshaller<T, V> extends FieldReadable<V>, Writable<T> {

	/**
	 * Returns a builder for use with {@link #readFrom(ProtoStreamReader, int, WireType, Object)}.
	 * May return a shared instance, if the builder type is immutable, or a new instance, if the builder is mutable.
	 * @return a builder.
	 */
	V createInitialValue();

	/**
	 * Builds the target object from the read value.
	 * @param value a read value
	 * @return the target object
	 */
	T build(V value);

	/**
	 * Creates a marshaller that reads and writes only the fields of this field set.
	 * @return a new marshaller
	 */
	@SuppressWarnings("unchecked")
	default ProtoStreamMarshaller<T> asMarshaller() {
		return this.asMarshaller((Class<T>) this.build(this.createInitialValue()).getClass());
	}

	/**
	 * Creates a marshaller that reads and writes only the fields of this field set.
	 * @param targetClass the marshaller type
	 * @return a new marshaller
	 */
	default ProtoStreamMarshaller<T> asMarshaller(Class<T> targetClass) {
		FieldSetMarshaller<T, V> marshaller = this;
		return new ProtoStreamMarshaller<>() {
			@Override
			public Class<? extends T> getJavaClass() {
				return targetClass;
			}

			@Override
			public T readFrom(ProtoStreamReader reader) throws IOException {
				FieldSetReader<V> valueReader = reader.createFieldSetReader(marshaller, 1);
				V value = marshaller.createInitialValue();
				while (!reader.isAtEnd()) {
					int tag = reader.readTag();
					int index = WireType.getTagFieldNumber(tag);
					if (valueReader.contains(index)) {
						value = valueReader.readField(value);
					} else {
						reader.skipField(tag);
					}
				}
				return marshaller.build(value);
			}

			@Override
			public void writeTo(ProtoStreamWriter writer, T value) throws IOException {
				writer.createFieldSetWriter(marshaller, 1).writeFields(value);
			}
		};
	}

	/**
	 * A simple field set marshaller whose reader and writer types are the same
	 * @param <T> the marshaller type
	 */
	interface Simple<T> extends FieldSetMarshaller<T, T> {

		@Override
		default T build(T value) {
			return value;
		}
	}

	/**
	 * A marshaller using a map entry field set.
	 * @param <T> the marshaller type
	 * @param <K> the entry key type
	 * @param <V> the entry value type
	 */
	interface Entry<T, K, V> extends FieldSetMarshaller<T, Map.Entry<K, V>> {

		@Override
		default Map.Entry<K, V> createInitialValue() {
			return new AbstractMap.SimpleEntry<>(null, null);
		}
	}

	/**
	 * A field set marshaller whose reader type supplies the writer type.
	 * @param <T> the writer type
	 * @param <V> the reader type
	 */
	interface Supplied<T, V extends Supplier<T>> extends FieldSetMarshaller<T, V> {

		@Override
		default T build(V value) {
			return value.get();
		}
	}
}
