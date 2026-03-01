/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import java.util.function.Consumer;

import org.wildfly.clustering.function.Function;

/**
 * Encapsulates a read-only reference.
 * @author Paul Ferraro
 * @param <T> the reference type
 */
public interface Reference<T> {

	/**
	 * Returns a thread-safe reader of this reference.
	 * @return a thread-safe reader of this reference.
	 */
	Reader<T> getReader();

	/**
	 * Returns a reference to the specified value.
	 * @param <T> the reference type
	 * @param value the reference value
	 * @return a reference to the specified value.
	 */
	static <T> Reference<T> of(T value) {
		return of(new SimpleReferenceReader<>(value, Function.identity()));
	}

	/**
	 * Returns a reference with the specified reader.
	 * @param <T> the reference type
	 * @param reader the reference reader
	 * @return a reference with the specified reader.
	 */
	static <T> Reference<T> of(Reader<T> reader) {
		return new Reference<>() {
			@Override
			public Reader<T> getReader() {
				return reader;
			}
		};
	}

	/**
	 * A reader of an object reference.
	 * @param <V> the reader type
	 */
	interface Reader<V> extends java.util.function.Supplier<V> {
		/**
		 * Consumes the referenced value.
		 * @param reader a consumer of the referenced value
		 */
		default void read(java.util.function.Consumer<? super V> reader) {
			reader.accept(this.get());
		}

		/**
		 * Returns a mapped reader of this reference.
		 * @param <R> the mapped type
		 * @param mapper a mapping function
		 * @return a mapped reader of this reference.
		 */
		<R> Reader<R> map(java.util.function.Function<? super V, ? extends R> mapper);
	}

	/**
	 * A reference to a fixed value.
	 * @param <T> the reference type
	 * @param <V> the reader type
	 */
	class SimpleReferenceReader<T, V> implements Reference.Reader<V> {
		private final T value;
		private final java.util.function.Function<? super T, ? extends V> mapper;

		SimpleReferenceReader(T value, java.util.function.Function<? super T, ? extends V> mapper) {
			this.value = value;
			this.mapper = mapper;
		}

		@Override
		public void read(Consumer<? super V> reader) {
			reader.accept(this.mapper.apply(this.value));
		}

		@Override
		public <R> Reader<R> map(java.util.function.Function<? super V, ? extends R> mapper) {
			return new SimpleReferenceReader<>(this.value, this.mapper.andThen(mapper));
		}

		@Override
		public V get() {
			return this.mapper.apply(this.value);
		}
	}
}
