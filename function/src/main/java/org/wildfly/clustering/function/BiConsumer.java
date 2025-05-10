/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;

/**
 * An enhanced binary consumer.
 * @author Paul Ferraro
 * @param <T> the first parameter type
 * @param <U> the second parameter type
 */
public interface BiConsumer<T, U> extends java.util.function.BiConsumer<T, U> {

	BiConsumer<?, ?> EMPTY = new BiConsumer<>() {
		@Override
		public void accept(Object ignore1, Object ignore2) {
			// Do nothing
		}
	};

	@Override
	default BiConsumer<T, U> andThen(java.util.function.BiConsumer<? super T, ? super U> after) {
		return of(List.of(this, after));
	}

	/**
	 * Returns a consumer that processes this consumer with reversed parameter order.
	 * @return a consumer that processes this consumer with reversed parameter order.
	 */
	default BiConsumer<U, T> reverse() {
		return new BiConsumer<>() {
			@Override
			public void accept(U value1, T value2) {
				BiConsumer.this.accept(value2, value1);
			}
		};
	}

	/**
	 * Returns a consumer that performs no action.
	 * @param <T> the first consumed type
	 * @param <U> the second consumed type
	 * @return an empty consumer
	 */
	@SuppressWarnings("unchecked")
	static <T, U> BiConsumer<T, U> empty() {
		return (BiConsumer<T, U>) EMPTY;
	}

	/**
	 * Returns a composite consumer that delegates to zero or more consumers.
	 * @param <T> the first consumed type
	 * @param <U> the second consumed type
	 * @param consumers zero or more consumers
	 * @return a composite consumer
	 */
	static <T, U> BiConsumer<T, U> of(Iterable<java.util.function.BiConsumer<? super T, ? super U>> consumers) {
		return new BiConsumer<>() {
			@Override
			public void accept(T value1, U value2) {
				for (java.util.function.BiConsumer<? super T, ? super U> consumer : consumers) {
					consumer.accept(value1, value2);
				}
			}
		};
	}

	/**
	 * Returns a consumer that delegates to a consumer of the first parameter, ignoring the second.
	 * @param <T> the first consumed type
	 * @param <U> the second consumed type
	 * @param consumer the consumer of the first parameter
	 * @return a consumer of the first parameter
	 */
	static <T, U> BiConsumer<T, U> former(java.util.function.Consumer<? super T> consumer) {
		return of(consumer, Consumer.empty());
	}

	/**
	 * Returns a consumer that delegates to a consumer of the second parameter, ignoring the first.
	 * @param <T> the first consumed type
	 * @param <U> the second consumed type
	 * @param consumer the consumer of the second parameter
	 * @return a consumer that delegates to a consumer of the second parameter, ignoring the first.
	 */
	static <T, U> BiConsumer<T, U> latter(java.util.function.Consumer<? super U> consumer) {
		return of(Consumer.empty(), consumer);
	}

	/**
	 * Returns a composite consumer that delegates to a consumer per parameter.
	 * @param <T> the first consumed type
	 * @param <U> the second consumed type
	 * @param consumer1 the consumer of the first parameter
	 * @param consumer2 the consumer of the second parameter
	 * @return a composite consumer
	 */
	static <T, U> BiConsumer<T, U> of(java.util.function.Consumer<? super T> consumer1, java.util.function.Consumer<? super U> consumer2) {
		return new BiConsumer<>() {
			@Override
			public void accept(T value1, U value2) {
				consumer1.accept(value1);
				consumer2.accept(value2);
			}
		};
	}
}
