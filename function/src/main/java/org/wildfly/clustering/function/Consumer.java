/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;
import java.util.function.Function;

/**
 * An enhanced unary consumer.
 * @author Paul Ferraro
 * @param <T> the accepted type
 */
public interface Consumer<T> extends java.util.function.Consumer<T> {

	@Override
	default Consumer<T> andThen(java.util.function.Consumer<? super T> after) {
		return of(List.of(this, after));
	}

	/**
	 * Returns a mapped consumer, that invokes this consumer using result of the specified function.
	 * @param <V> the mapped type
	 * @param mapper a mapping function
	 * @return a mapped consumer
	 */
	default <V> Consumer<V> map(Function<V, T> mapper) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				Consumer.this.accept(mapper.apply(value));
			}
		};
	}

	Consumer<?> EMPTY = new Consumer<>() {
		@Override
		public void accept(Object value) {
		}
	};

	/**
	 * Returns a consumer that performs no action.
	 * @param <V> the consumed type
	 * @return an empty consumer
	 */
	@SuppressWarnings("unchecked")
	static <V> Consumer<V> empty() {
		return (Consumer<V>) EMPTY;
	}

	/**
	 * Returns a consumer that runs the specified task, ignoring its parameter.
	 * @param <V> the ignored parameter type
	 * @param task a runnable task
	 * @return a consumer that runs the specified task, ignoring its parameter.
	 */
	static <V> Consumer<V> of(java.lang.Runnable task) {
		return new Consumer<>() {
			@Override
			public void accept(V ignored) {
				task.run();
			}
		};
	}

	/**
	 * Returns a composite consumer that delegates to zero or more consumers.
	 * @param <V> the consumed type
	 * @param consumers zero or more consumers
	 * @return a composite consumer
	 */
	static <V> Consumer<V> of(Iterable<java.util.function.Consumer<? super V>> consumers) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				for (java.util.function.Consumer<? super V> consumer : consumers) {
					consumer.accept(value);
				}
			}
		};
	}
}
