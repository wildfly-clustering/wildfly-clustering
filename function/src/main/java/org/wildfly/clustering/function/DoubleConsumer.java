/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

/**
 * An enhanced long consumer.
 * @author Paul Ferraro
 */
public interface DoubleConsumer extends java.util.function.DoubleConsumer {
	/** Consumer that discards its parameter */
	DoubleConsumer EMPTY = value -> {};

	@Override
	default DoubleConsumer andThen(java.util.function.DoubleConsumer after) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
				DoubleConsumer.this.accept(value);
				after.accept(value);
			}
		};
	}

	/**
	 * Returns a boxed version of this consumer.
	 * @return a boxed version of this consumer.
	 */
	default Consumer<Double> boxed() {
		return this.compose(Double::doubleValue);
	}

	/**
	 * Returns a consumer that conditionally invokes this consumer when allowed by the specified predicate.
	 * @param predicate a predicate that determines whether or not to invoke this consumer
	 * @return a consumer that conditionally invokes this consumer when allowed by the specified predicate.
	 */
	default DoubleConsumer when(java.util.function.DoublePredicate predicate) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
				if (predicate.test(value)) {
					DoubleConsumer.this.accept(value);
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
	default DoubleConsumer withDefault(java.util.function.DoublePredicate predicate, java.util.function.DoubleSupplier defaultValue) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
				DoubleConsumer.this.accept(predicate.test(value) ? value : defaultValue.getAsDouble());
			}
		};
	}

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param composer a composing function
	 * @return a composed consumer
	 */
	default DoubleConsumer composeAsDouble(java.util.function.DoubleUnaryOperator composer) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
				DoubleConsumer.this.accept(composer.applyAsDouble(value));
			}
		};
	}

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param <V> the mapped type
	 * @param composer a composing function
	 * @return a composed consumer
	 */
	default <V> Consumer<V> compose(java.util.function.ToDoubleFunction<V> composer) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				DoubleConsumer.this.accept(composer.applyAsDouble(value));
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
	default <V1, V2> BiConsumer<V1, V2> compose(java.util.function.ToDoubleBiFunction<V1, V2> composer) {
		return new BiConsumer<>() {
			@Override
			public void accept(V1 value1, V2 value2) {
				DoubleConsumer.this.accept(composer.applyAsDouble(value1, value2));
			}
		};
	}

	/**
	 * Returns a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 * @param factory a factory of the function return value
	 * @param <R> the return type
	 * @return a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 */
	default <R> DoubleFunction<R> thenReturn(java.util.function.Supplier<R> factory) {
		return new DoubleFunction<>() {
			@Override
			public R apply(double value) {
				DoubleConsumer.this.accept(value);
				return factory.get();
			}
		};
	}

	/**
	 * Returns a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 * @param factory a factory of the function return value
	 * @return a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 */
	default DoubleUnaryOperator thenReturnDouble(java.util.function.DoubleSupplier factory) {
		return new DoubleUnaryOperator() {
			@Override
			public double applyAsDouble(double value) {
				DoubleConsumer.this.accept(value);
				return factory.getAsDouble();
			}
		};
	}

	/**
	 * Returns a composite consumer that delegates to the specified consumers.
	 * @param consumers a number of consumers
	 * @return a composite consumer
	 */
	static DoubleConsumer acceptAll(Iterable<? extends DoubleConsumer> consumers) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
				for (DoubleConsumer consumer : consumers) {
					consumer.accept(value);
				}
			}
		};
	}
}
