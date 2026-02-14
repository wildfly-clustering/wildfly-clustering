/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced {@link java.util.function.IntFunction}.
 * @author Paul Ferraro
 * @param <V> the return type
 */
public interface IntFunction<V> extends java.util.function.IntFunction<V>, IntOperation, PrimitiveFunction<Integer, V> {

	@Override
	default Function<Integer, V> box() {
		return this.compose(IntUnaryOperator.identity().box());
	}

	@Override
	default <T> Function<T, V> compose(java.util.function.ToIntFunction<? super T> before) {
		return Function.of(before, this);
	}

	@Override
	default <T1, T2> BiFunction<T1, T2, V> composeBinary(java.util.function.ToIntBiFunction<? super T1, ? super T2> before) {
		return BiFunction.of(before, this);
	}

	@Override
	default BooleanFunction<V> composeBoolean(BooleanToIntFunction before) {
		return BooleanFunction.of(before, this);
	}

	@Override
	default DoubleFunction<V> composeDouble(java.util.function.DoubleToIntFunction before) {
		return DoubleFunction.of(before, this);
	}

	@Override
	default IntFunction<V> composeInt(java.util.function.IntUnaryOperator before) {
		return IntFunction.of(before, this);
	}

	@Override
	default LongFunction<V> composeLong(java.util.function.LongToIntFunction before) {
		return LongFunction.of(before, this);
	}

	@Override
	default IntConsumer thenAccept(java.util.function.Consumer<? super V> after) {
		return IntConsumer.of(this, after);
	}

	@Override
	default <R> IntFunction<R> thenApply(java.util.function.Function<? super V, ? extends R> after) {
		return IntFunction.of(this, after);
	}

	@Override
	default IntToDoubleFunction thenApplyAsDouble(java.util.function.ToDoubleFunction<? super V> after) {
		return IntToDoubleFunction.of(this, after);
	}

	@Override
	default IntUnaryOperator thenApplyAsInt(java.util.function.ToIntFunction<? super V> after) {
		return IntUnaryOperator.of(this, after);
	}

	@Override
	default IntToLongFunction thenApplyAsLong(java.util.function.ToLongFunction<? super V> after) {
		return IntToLongFunction.of(this, after);
	}

	@Override
	default IntPredicate thenTest(java.util.function.Predicate<? super V> after) {
		return IntPredicate.of(this, after);
	}

	@Override
	default IntFunction<V> thenThrow(java.util.function.Function<? super V, ? extends RuntimeException> exception) {
		return new IntFunction<>() {
			@Override
			public V apply(int value) {
				throw exception.apply(IntFunction.this.apply(value));
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
	static <R> IntFunction<R> of(R result) {
		return (result != null) ? new SimpleIntFunction<>(result) : (IntFunction<R>) SimpleIntFunction.NULL;
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <R> IntFunction<R> of(java.util.function.IntConsumer before, java.util.function.Supplier<? extends R> after) {
		return new IntFunction<>() {
			@Override
			public R apply(int value) {
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
	static <T, R> IntFunction<R> of(java.util.function.IntFunction<? extends T> before, java.util.function.Function<? super T, ? extends R> after) {
		return new IntFunction<>() {
			@Override
			public R apply(int value) {
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
	static <R> IntFunction<R> of(java.util.function.IntPredicate before, BooleanFunction<? extends R> after) {
		return new IntFunction<>() {
			@Override
			public R apply(int value) {
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
	static <R> IntFunction<R> of(java.util.function.IntToDoubleFunction before, java.util.function.DoubleFunction<? extends R> after) {
		return new IntFunction<>() {
			@Override
			public R apply(int value) {
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
	static <R> IntFunction<R> of(java.util.function.IntUnaryOperator before, java.util.function.IntFunction<? extends R> after) {
		return new IntFunction<>() {
			@Override
			public R apply(int value) {
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
	static <R> IntFunction<R> of(java.util.function.IntToLongFunction before, java.util.function.LongFunction<? extends R> after) {
		return new IntFunction<>() {
			@Override
			public R apply(int value) {
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
	static <R> IntFunction<R> when(java.util.function.IntPredicate predicate, java.util.function.IntFunction<? extends R> accepted, java.util.function.IntFunction<? extends R> rejected) {
		return new IntFunction<>() {
			@Override
			public R apply(int value) {
				java.util.function.IntFunction<? extends R> function = predicate.test(value) ? accepted : rejected;
				return function.apply(value);
			}
		};
	}

	/**
	 * A simple function returning a fixed value.
	 * @param <V> the fixed value
	 */
	class SimpleIntFunction<V> implements IntFunction<V> {
		static final IntFunction<?> NULL = new SimpleIntFunction<>(null);

		private final V value;

		SimpleIntFunction(V value) {
			this.value = value;
		}

		@Override
		public V apply(int value) {
			return this.value;
		}

		@Override
		public Function<Integer, V> box() {
			return Function.of(this.value);
		}
	}
}
