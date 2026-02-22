/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import java.util.function.Consumer;

import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;

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
		return new SimpleReference<>(value);
	}

	/**
	 * A reader of an object reference.
	 * @param <T> the referenced object type
	 */
	interface Reader<T> {
		/**
		 * Consumes the referenced value.
		 * @param reader a consumer of the referenced value
		 */
		default void consume(java.util.function.Consumer<T> reader) {
			this.read(Function.of(reader, Supplier.of(null)));
		}

		/**
		 * Applies a function to the referenced value.
		 * @param <R> the function return type
		 * @param reader a function applied to the referenced value
		 * @return the result of the specified function
		 */
		<R> R read(java.util.function.Function<T, R> reader);
	}

	/**
	 * A reference to a fixed value.
	 * @param <T> the reference type
	 */
	class SimpleReference<T> implements Reference<T>, Reference.Reader<T> {
		private final T value;

		SimpleReference(T value) {
			this.value = value;
		}

		@Override
		public void consume(Consumer<T> reader) {
			reader.accept(this.value);
		}

		@Override
		public <R> R read(java.util.function.Function<T, R> reader) {
			return reader.apply(this.value);
		}

		@Override
		public Reader<T> getReader() {
			return this;
		}
	}
}
