/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

/**
 * An enhanced integer consumer.
 * @author Paul Ferraro
 */
public interface IntConsumer extends java.util.function.IntConsumer {
	/** Consumer that discards its parameter */
	IntConsumer EMPTY = value -> {};

	@Override
	default IntConsumer andThen(java.util.function.IntConsumer after) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				IntConsumer.this.accept(value);
				after.accept(value);
			}
		};
	}

	/**
	 * Returns a boxed version of this consumer.
	 * @return a boxed version of this consumer.
	 */
	default Consumer<Integer> boxed() {
		return this.compose(Integer::intValue);
	}

	/**
	 * Returns a consumer that conditionally invokes this consumer when allowed by the specified predicate.
	 * @param predicate a predicate that determines whether or not to invoke this consumer
	 * @return a consumer that conditionally invokes this consumer when allowed by the specified predicate.
	 */
	default IntConsumer when(java.util.function.IntPredicate predicate) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				if (predicate.test(value)) {
					IntConsumer.this.accept(value);
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
	default IntConsumer withDefault(java.util.function.IntPredicate predicate, java.util.function.IntSupplier defaultValue) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				IntConsumer.this.accept(predicate.test(value) ? value : defaultValue.getAsInt());
			}
		};
	}

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param composer a composing function
	 * @return a composed consumer
	 */
	default IntConsumer composeAsInt(java.util.function.IntUnaryOperator composer) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				IntConsumer.this.accept(composer.applyAsInt(value));
			}
		};
	}

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param <V> the mapped type
	 * @param composer a composing function
	 * @return a composed consumer
	 */
	default <V> Consumer<V> compose(java.util.function.ToIntFunction<V> composer) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				IntConsumer.this.accept(composer.applyAsInt(value));
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
	default <V1, V2> BiConsumer<V1, V2> compose(java.util.function.ToIntBiFunction<V1, V2> composer) {
		return new BiConsumer<>() {
			@Override
			public void accept(V1 value1, V2 value2) {
				IntConsumer.this.accept(composer.applyAsInt(value1, value2));
			}
		};
	}

	/**
	 * Returns a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 * @param factory a factory of the function return value
	 * @param <R> the return type
	 * @return a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 */
	default <R> IntFunction<R> thenReturn(java.util.function.Supplier<R> factory) {
		return new IntFunction<>() {
			@Override
			public R apply(int value) {
				IntConsumer.this.accept(value);
				return factory.get();
			}
		};
	}

	/**
	 * Returns a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 * @param factory a factory of the function return value
	 * @return a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 */
	default IntUnaryOperator thenReturnInt(java.util.function.IntSupplier factory) {
		return new IntUnaryOperator() {
			@Override
			public int applyAsInt(int value) {
				IntConsumer.this.accept(value);
				return factory.getAsInt();
			}
		};
	}

	/**
	 * Returns a composite consumer that delegates to the specified consumers.
	 * @param consumers a number of consumers
	 * @return a composite consumer
	 */
	static IntConsumer acceptAll(Iterable<? extends IntConsumer> consumers) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				for (IntConsumer consumer : consumers) {
					consumer.accept(value);
				}
			}
		};
	}
}
