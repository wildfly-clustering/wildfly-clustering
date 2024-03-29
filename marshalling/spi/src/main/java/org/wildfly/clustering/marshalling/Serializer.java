/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.OptionalInt;

/**
 * Writes/reads an object to/from a binary stream.
 * @param <T> the target type of this serializer.
 * @author Paul Ferraro
 */
public interface Serializer<T> {

	/**
	 * Writes the specified object to the specified output stream
	 * @param output the data output stream
	 * @param value an object to serialize
	 * @throws IOException if an I/O error occurs
	 */
	void write(DataOutput output, T value) throws IOException;

	/**
	 * Reads an object from the specified input stream.
	 * @param input a data input stream
	 * @return the deserialized object
	 * @throws IOException if an I/O error occurs
	 */
	T read(DataInput input) throws IOException;

	/**
	 * Returns the size of the buffer to use for marshalling the specified object, if known.
	 * @param object the object to be sized
	 * @return the buffer size (in bytes), or empty if unknown.
	 */
	default OptionalInt size(T object) {
		return OptionalInt.empty();
	}

	static <T> Serializer<T> of(T value) {
		return new Serializer<>() {
			@Override
			public void write(DataOutput output, T value) throws IOException {
			}

			@Override
			public T read(DataInput input) throws IOException {
				return value;
			}
		};
	}
}
