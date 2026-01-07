/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function with a single double parameter.
 * @author Paul Ferraro
 * @param <V> the return type
 */
public interface DoubleFunction<V> extends java.util.function.DoubleFunction<V>, DoubleOperation, PrimitiveFunction<Double, V> {

	@Override
	default Function<Double, V> box() {
		return this.compose(DoubleUnaryOperator.identity().box());
	}

	@Override
	default <T> Function<T, V> compose(java.util.function.ToDoubleFunction<? super T> before) {
		return Function.of(before, this);
	}

	@Override
	default <T1, T2> BiFunction<T1, T2, V> composeBinary(java.util.function.ToDoubleBiFunction<? super T1, ? super T2> before) {
		return BiFunction.of(before, this);
	}

	@Override
	default BooleanFunction<V> composeBoolean(BooleanToDoubleFunction before) {
		return BooleanFunction.of(before, this);
	}

	@Override
	default DoubleFunction<V> composeDouble(java.util.function.DoubleUnaryOperator before) {
		return DoubleFunction.of(before, this);
	}

	@Override
	default IntFunction<V> composeInt(java.util.function.IntToDoubleFunction before) {
		return IntFunction.of(before, this);
	}

	@Override
	default LongFunction<V> composeLong(java.util.function.LongToDoubleFunction before) {
		return LongFunction.of(before, this);
	}

	@Override
	default DoubleConsumer thenAccept(java.util.function.Consumer<? super V> after) {
		return DoubleConsumer.of(this, after);
	}

	@Override
	default <R> DoubleFunction<R> thenApply(java.util.function.Function<? super V, ? extends R> after) {
		return DoubleFunction.of(this, after);
	}

	@Override
	default DoubleUnaryOperator thenApplyAsDouble(java.util.function.ToDoubleFunction<? super V> after) {
		return DoubleUnaryOperator.of(this, after);
	}

	@Override
	default DoubleToIntFunction thenApplyAsInt(java.util.function.ToIntFunction<? super V> after) {
		return DoubleToIntFunction.of(this, after);
	}

	@Override
	default DoubleToLongFunction thenApplyAsLong(java.util.function.ToLongFunction<? super V> after) {
		return DoubleToLongFunction.of(this, after);
	}

	@Override
	default DoublePredicate thenTest(java.util.function.Predicate<? super V> after) {
		return DoublePredicate.of(this, after);
	}

	@Override
	default DoubleFunction<V> thenThrow(java.util.function.Function<? super V, ? extends RuntimeException> exception) {
		return new DoubleFunction<>() {
			@Override
			public V apply(double value) {
				throw exception.apply(DoubleFunction.this.apply(value));
			}
		};
	}

	/**
	 * Returns a function that always returns the specified value, ignoring its parameter.
	 * @param <R> the function return type
	 * @param result the function result
	 * @return a function that always returns the specified value, ignoring its parameter.
	 */
	@SuppressWarnings("unchecked")
	static <R> DoubleFunction<R> of(R result) {
		return (result != null) ? new SimpleDoubleFunction<>(result) : (DoubleFunction<R>) SimpleDoubleFunction.NULL;
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <R> DoubleFunction<R> of(java.util.function.DoubleConsumer before, java.util.function.Supplier<? extends R> after) {
		return new DoubleFunction<>() {
			@Override
			public R apply(double value) {
				before.accept(value);
				return after.get();
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the intermediate type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T, R> DoubleFunction<R> of(java.util.function.DoubleFunction<? extends T> before, java.util.function.Function<? super T, ? extends R> after) {
		return new DoubleFunction<>() {
			@Override
			public R apply(double value) {
				return after.apply(before.apply(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <R> DoubleFunction<R> of(java.util.function.DoublePredicate before, BooleanFunction<? extends R> after) {
		return new DoubleFunction<>() {
			@Override
			public R apply(double value) {
				return after.apply(before.test(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <R> DoubleFunction<R> of(java.util.function.DoubleUnaryOperator before, java.util.function.DoubleFunction<? extends R> after) {
		return new DoubleFunction<>() {
			@Override
			public R apply(double value) {
				return after.apply(before.applyAsDouble(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <R> DoubleFunction<R> of(java.util.function.DoubleToIntFunction before, java.util.function.IntFunction<? extends R> after) {
		return new DoubleFunction<>() {
			@Override
			public R apply(double value) {
				return after.apply(before.applyAsInt(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <R> DoubleFunction<R> of(java.util.function.DoubleToLongFunction before, java.util.function.LongFunction<? extends R> after) {
		return new DoubleFunction<>() {
			@Override
			public R apply(double value) {
				return after.apply(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a function that delegates to one of two functions based on the specified predicate.
	 * @param <R> the function return type
	 * @param predicate a predicate
	 * @param accepted the function to apply when accepted by the specified predicate
	 * @param rejected the function to apply when rejected by the specified predicate
	 * @return a function that delegates to one of two functions based on the specified predicate.
	 */
	static <R> DoubleFunction<R> when(java.util.function.DoublePredicate predicate, java.util.function.DoubleFunction<? extends R> accepted, java.util.function.DoubleFunction<? extends R> rejected) {
		return new DoubleFunction<>() {
			@Override
			public R apply(double value) {
				java.util.function.DoubleFunction<? extends R> function = predicate.test(value) ? accepted : rejected;
				return function.apply(value);
			}
		};
	}

	/**
	 * A simple function returning a fixed value.
	 * @param <V> the fixed value
	 */
	class SimpleDoubleFunction<V> implements DoubleFunction<V> {
		static final DoubleFunction<?> NULL = new SimpleDoubleFunction<>(null);

		private final V value;

		SimpleDoubleFunction(V value) {
			this.value = value;
		}

		@Override
		public V apply(double value) {
			return this.value;
		}

		@Override
		public Function<Double, V> box() {
			return Function.of(this.value);
		}
	}
}
