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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Formats an objects into a string representation and back again.
 * @param <T> the formatted type
 * @author Paul Ferraro
 */
public interface Formatter<T> {
	/**
	 * Returns the type of the formatted object.
	 * @return the type of the formatted object.
	 */
	Class<? extends T> getType();

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
	 * @param type the wrapped type
	 * @param unwrapper the unwrapper function
	 * @param wrapper the wrapper function
	 * @return a wrapped formmater
	 */
	default <U> Formatter<U> wrap(Class<? extends U> type, Function<U, T> unwrapper, Function<T, U> wrapper) {
		Formatter<T> formatter = this;
		return new Formatter<>() {
			@Override
			public Class<? extends U> getType() {
				return type;
			}

			@Override
			public U parse(String value) {
				return wrapper.apply(formatter.parse(value));
			}

			@Override
			public String format(U value) {
				return formatter.format(unwrapper.apply(value));
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
			return this.wrap(targetClass, Object::toString, wrapper);
		}
	}

	Identity IDENTITY = new Identity() {
		@Override
		public Class<String> getType() {
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
			public Class<T> getType() {
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

	static <T> Formatter<T> serialized(Class<? extends T> type, Serializer<T> serializer) {
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

	static <T, V1, V2> Formatter<T> joining(Class<? extends T> type, String delimiter, Formatter<V1> formatter1, Formatter<V2> formatter2, Function<T, V1> unwrapper1, Function<T, V2> unwrapper2, BiFunction<V1, V2, T> wrapper) {
		return new Formatter<>() {
			@Override
			public Class<? extends T> getType() {
				return type;
			}

			@Override
			public T parse(String value) {
				String[] values = value.split(Pattern.quote(delimiter));
				return wrapper.apply(formatter1.parse(values[0]), formatter2.parse(values[1]));
			}

			@Override
			public String format(T value) {
				return String.join(delimiter, formatter1.format(unwrapper1.apply(value)), formatter2.format(unwrapper2.apply(value)));
			}
		};
	}

	static <T> Formatter<T> joining(Class<? extends T> type, String delimiter, Function<T, String[]> unwrapper, Function<String[], T> wrapper) {
		return new Formatter<>() {
			@Override
			public Class<? extends T> getType() {
				return type;
			}

			@Override
			public T parse(String value) {
				return wrapper.apply(value.split(Pattern.quote(delimiter)));
			}

			@Override
			public String format(T value) {
				return String.join(delimiter, unwrapper.apply(value));
			}
		};
	}

	class Provided<T> implements Formatter<T> {
		private final Formatter<T> formatter;

		public Provided(Formatter<T> formatter) {
			this.formatter = formatter;
		}

		@Override
		public Class<? extends T> getType() {
			return this.formatter.getType();
		}

		@Override
		public T parse(String value) {
			return this.formatter.parse(value);
		}

		@Override
		public String format(T value) {
			return this.formatter.format(value);
		}

		@Override
		public <U> Formatter<U> wrap(Class<? extends U> type, Function<U, T> unwrapper, Function<T, U> wrapper) {
			return this.formatter.wrap(type, unwrapper, wrapper);
		}
	}
}
