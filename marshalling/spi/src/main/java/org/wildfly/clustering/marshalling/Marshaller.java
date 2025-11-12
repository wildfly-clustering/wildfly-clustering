/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling;

import java.io.IOException;

import org.wildfly.clustering.function.Predicate;

/**
 * Marshals an object to and from its serialized form.
 * @author Paul Ferraro
 * @param <V> the value type
 * @param <S> the marshalled type
 */
public interface Marshaller<V, S> extends Predicate<Object> {

	/**
	 * An identity marshaller that does no marshalling.
	 * @param <T> the value type
	 * @return an identity marshaller
	 */
	static <T> Marshaller<T, T> identity() {
		return new Marshaller<>() {
			@Override
			public boolean test(Object object) {
				return true;
			}

			@Override
			public T read(T value) {
				return value;
			}

			@Override
			public T write(T value) {
				return value;
			}
		};
	}

	/**
	 * Reads a value from its marshalled form.
	 * @param value the marshalled form
	 * @return an unmarshalled value
	 * @throws IOException if the value could not be read
	 */
	V read(S value) throws IOException;

	/**
	 * Writes a value to its serialized form
	 * @param value a value to marshal.
	 * @return the serialized form of the value
	 * @throws IOException if the value could not be written
	 */
	S write(V value) throws IOException;
}
