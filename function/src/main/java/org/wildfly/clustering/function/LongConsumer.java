/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

/**
 * An enhanced long consumer.
 * @author Paul Ferraro
 */
public interface LongConsumer extends java.util.function.LongConsumer {
	/** Consumer that discards its parameter */
	LongConsumer EMPTY = value -> {};

	@Override
	default LongConsumer andThen(java.util.function.LongConsumer after) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
				LongConsumer.this.accept(value);
				after.accept(value);
			}
		};
	}

	/**
	 * Returns a boxed version of this consumer.
	 * @return a boxed version of this consumer.
	 */
	default Consumer<Long> boxed() {
		return this.compose(Long::longValue);
	}

	/**
	 * Returns a consumer that conditionally invokes this consumer when allowed by the specified predicate.
	 * @param predicate a predicate that determines whether or not to invoke this consumer
	 * @return a consumer that conditionally invokes this consumer when allowed by the specified predicate.
	 */
	default LongConsumer when(java.util.function.LongPredicate predicate) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
				if (predicate.test(value)) {
					LongConsumer.this.accept(value);
				}
			}
		};
	}

	/**
	 * Returns a consumer that accepts the value returned by the specified default provider if its value does not match the specified predicate.
	 * @param predicate a predicate used to determine the parameter of this consumer
	 * @param defaultValue a provider of the default parameter value
	 * @return a consumer that accepts the value returned by the specified default provider if its value does not match the specified predicate.
	 */
	default LongConsumer withDefault(java.util.function.LongPredicate predicate, java.util.function.LongSupplier defaultValue) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
				LongConsumer.this.accept(predicate.test(value) ? value : defaultValue.getAsLong());
			}
		};
	}

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param composer a composing function
	 * @return a composed consumer
	 */
	default LongConsumer composeAsLong(java.util.function.LongUnaryOperator composer) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
				LongConsumer.this.accept(composer.applyAsLong(value));
			}
		};
	}

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param <V> the mapped type
	 * @param composer a composing function
	 * @return a composed consumer
	 */
	default <V> Consumer<V> compose(java.util.function.ToLongFunction<V> composer) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				LongConsumer.this.accept(composer.applyAsLong(value));
			}
		};
	}

	/**
	 * Composes a binary consumer that invokes this consumer using result of the specified binary function.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param composer a composing function
	 * @return a binary consumer that invokes this consumer using result of the specified binary function.
	 */
	default <V1, V2> BiConsumer<V1, V2> compose(java.util.function.ToLongBiFunction<V1, V2> composer) {
		return new BiConsumer<>() {
			@Override
			public void accept(V1 value1, V2 value2) {
				LongConsumer.this.accept(composer.applyAsLong(value1, value2));
			}
		};
	}

	/**
	 * Returns a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 * @param factory a factory of the function return value
	 * @param <R> the return type
	 * @return a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 */
	default <R> LongFunction<R> thenReturn(java.util.function.Supplier<R> factory) {
		return new LongFunction<>() {
			@Override
			public R apply(long value) {
				LongConsumer.this.accept(value);
				return factory.get();
			}
		};
	}

	/**
	 * Returns a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 * @param factory a factory of the function return value
	 * @return a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 */
	default LongUnaryOperator thenReturnLong(java.util.function.LongSupplier factory) {
		return new LongUnaryOperator() {
			@Override
			public long applyAsLong(long value) {
				LongConsumer.this.accept(value);
				return factory.getAsLong();
			}
		};
	}

	/**
	 * Returns a composite consumer that delegates to the specified consumers.
	 * @param consumers a number of consumers
	 * @return a composite consumer
	 */
	static LongConsumer acceptAll(Iterable<? extends LongConsumer> consumers) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
				for (LongConsumer consumer : consumers) {
					consumer.accept(value);
				}
			}
		};
	}
}
