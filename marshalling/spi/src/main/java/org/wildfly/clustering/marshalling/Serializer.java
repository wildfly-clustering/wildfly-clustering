/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.OptionalInt;
import java.util.function.Function;

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

	/**
	 * Creates a wrapped serializer that delegates to this serializer applying the specified wrapping/unwrapping functions.
	 * @param <V> the wrapper type
	 * @param unwrapper an unwrapping function
	 * @param wrapper a wrapping function
	 * @return a wrapped serializer
	 */
	default <V> Serializer<V> wrap(Function<V, T> unwrapper, Function<T, V> wrapper) {
		Serializer<T> serializer = this;
		return new Serializer<>() {
			@Override
			public void write(DataOutput output, V value) throws IOException {
				serializer.write(output, unwrapper.apply(value));
			}

			@Override
			public V read(DataInput input) throws IOException {
				return wrapper.apply(serializer.read(input));
			}

			@Override
			public OptionalInt size(V object) {
				return serializer.size(unwrapper.apply(object));
			}
		};
	}

	/**
	 * Creates a {@link Formatter} based on this serializer.
	 * @param type the serialized type
	 * @return a new formatter
	 */
	default Formatter<T> toFormatter(Class<? extends T> type) {
		Serializer<T> serializer = this;
		return new Formatter<>() {
			@Override
			public Class<? extends T> getType() {
				return type;
			}

			@Override
			public T parse(String value) {
				byte[] bytes = Base64.getDecoder().decode(value);
				try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
					return serializer.read(input);
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
			}

			@Override
			public String format(T key) {
				ByteArrayOutputStream bytes = new ByteArrayOutputStream(serializer.size(key).orElse(64));
				try (DataOutputStream output = new DataOutputStream(bytes)) {
					serializer.write(output, key);
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
				return Base64.getEncoder().encodeToString(bytes.toByteArray());
			}
		};
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

	class Provided<T> implements Serializer<T> {
		private final Serializer<T> serializer;

		public Provided(Serializer<T> serializer) {
			this.serializer = serializer;
		}

		@Override
		public void write(DataOutput output, T value) throws IOException {
			this.serializer.write(output, value);
		}

		@Override
		public T read(DataInput input) throws IOException {
			return this.serializer.read(input);
		}

		@Override
		public OptionalInt size(T object) {
			return this.serializer.size(object);
		}

		@Override
		public <V> Serializer<V> wrap(Function<V, T> unwrapper, Function<T, V> wrapper) {
			return this.serializer.wrap(unwrapper, wrapper);
		}
	}
}
