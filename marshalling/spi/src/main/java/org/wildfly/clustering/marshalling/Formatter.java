/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.function.Function;

/**
 * Formats an objects into a string representation and back again.
 * @param <T> the formatted type
 * @author Paul Ferraro
 */
public interface Formatter<T> {
	/**
	 * The implementation class of the target key of this format.
	 * @return an implementation class
	 */
	Class<T> getTargetClass();

	/**
	 * Parses the key from the specified string.
	 * @param value a string representation of the key
	 * @return the parsed key
	 */
	T parse(String value);

	/**
	 * Formats the specified key to a string representation.
	 * @param value a key to format
	 * @return a string representation of the specified key.
	 */
	String format(T value);

	/**
	 * Returns a wrapped formatter.
	 * @param <U> the wrapped type
	 * @param targetClass the wrapped type
	 * @param wrapper the wrapper function
	 * @param unwrapper the unwrapper function
	 * @return a wrapped formmater
	 */
	default <U> Formatter<U> wrap(Class<U> targetClass, Function<T, U> wrapper, Function<U, T> unwrapper) {
		return new Formatter<>() {
			@Override
			public Class<U> getTargetClass() {
				return targetClass;
			}

			@Override
			public U parse(String value) {
				return wrapper.apply(Formatter.this.parse(value));
			}

			@Override
			public String format(U value) {
				return Formatter.this.format(unwrapper.apply(value));
			}
		};
	}

	interface Identity extends Formatter<String> {
		/**
		 * Returns a wrapping formatter
		 * @param <U> the wrapped type
		 * @param targetClass the wrapped type
		 * @param wrapper the wrapper function
		 * @return a wrapped formmater
		 */
		default <U> Formatter<U> wrap(Class<U> targetClass, Function<String, U> wrapper) {
			return this.wrap(targetClass, wrapper, Object::toString);
		}
	}

	Identity IDENTITY = new Identity() {
		@Override
		public Class<String> getTargetClass() {
			return String.class;
		}

		@Override
		public String parse(String value) {
			return value;
		}

		@Override
		public String format(String key) {
			return key;
		}
	};

	static <T> Formatter<T> of(T value) {
		return new Formatter<>() {
			@SuppressWarnings("unchecked")
			@Override
			public Class<T> getTargetClass() {
				return (Class<T>) value.getClass();
			}

			@Override
			public T parse(String ignored) {
				return value;
			}

			@Override
			public String format(T ignored) {
				return "";
			}
		};
	}

	static <T> Formatter<T> serialized(Class<T> targetClass, Serializer<T> serializer) {
		return new Formatter<>() {
			@Override
			public Class<T> getTargetClass() {
				return targetClass;
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
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				try (DataOutputStream output = new DataOutputStream(bytes)) {
					serializer.write(output, key);
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
				return Base64.getEncoder().encodeToString(bytes.toByteArray());
			}
		};
	}

	class Provided<T> implements Formatter<T> {
		private final Formatter<T> formatter;

		public Provided(Formatter<T> formatter) {
			this.formatter = formatter;
		}

		@Override
		public Class<T> getTargetClass() {
			return this.formatter.getTargetClass();
		}

		@Override
		public T parse(String value) {
			return this.formatter.parse(value);
		}

		@Override
		public String format(T value) {
			return this.formatter.format(value);
		}
	}
}
