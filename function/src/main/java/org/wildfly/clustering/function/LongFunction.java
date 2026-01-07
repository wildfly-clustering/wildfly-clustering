/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced {@link java.util.function.LongFunction}.
 * @author Paul Ferraro
 * @param <V> the return type
 */
public interface LongFunction<V> extends java.util.function.LongFunction<V>, LongOperation, PrimitiveFunction<Long, V> {

	@Override
	default Function<Long, V> box() {
		return this.compose(LongUnaryOperator.identity().box());
	}

	@Override
	default <T> Function<T, V> compose(java.util.function.ToLongFunction<? super T> before) {
		return Function.of(before, this);
	}

	@Override
	default <T1, T2> BiFunction<T1, T2, V> composeBinary(java.util.function.ToLongBiFunction<? super T1, ? super T2> before) {
		return BiFunction.of(before, this);
	}

	@Override
	default BooleanFunction<V> composeBoolean(BooleanToLongFunction before) {
		return BooleanFunction.of(before, this);
	}

	@Override
	default DoubleFunction<V> composeDouble(java.util.function.DoubleToLongFunction before) {
		return DoubleFunction.of(before, this);
	}

	@Override
	default IntFunction<V> composeInt(java.util.function.IntToLongFunction before) {
		return IntFunction.of(before, this);
	}

	@Override
	default LongFunction<V> composeLong(java.util.function.LongUnaryOperator before) {
		return LongFunction.of(before, this);
	}

	@Override
	default LongConsumer thenAccept(java.util.function.Consumer<? super V> after) {
		return LongConsumer.of(this, after);
	}

	@Override
	default <R> LongFunction<R> thenApply(java.util.function.Function<? super V, ? extends R> after) {
		return LongFunction.of(this, after);
	}

	@Override
	default LongToDoubleFunction thenApplyAsDouble(java.util.function.ToDoubleFunction<? super V> after) {
		return LongToDoubleFunction.of(this, after);
	}

	@Override
	default LongToIntFunction thenApplyAsInt(java.util.function.ToIntFunction<? super V> after) {
		return LongToIntFunction.of(this, after);
	}

	@Override
	default LongUnaryOperator thenApplyAsLong(java.util.function.ToLongFunction<? super V> after) {
		return LongUnaryOperator.of(this, after);
	}

	@Override
	default LongPredicate thenTest(java.util.function.Predicate<? super V> after) {
		return LongPredicate.of(this, after);
	}

	@Override
	default LongFunction<V> thenThrow(java.util.function.Function<? super V, ? extends RuntimeException> exception) {
		return new LongFunction<>() {
			@Override
			public V apply(long value) {
				throw exception.apply(LongFunction.this.apply(value));
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
	static <R> LongFunction<R> of(R result) {
		return (result != null) ? new SimpleLongFunction<>(result) : (LongFunction<R>) SimpleLongFunction.NULL;
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <R> LongFunction<R> of(java.util.function.LongConsumer before, java.util.function.Supplier<? extends R> after) {
		return new LongFunction<>() {
			@Override
			public R apply(long value) {
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
	static <T, R> LongFunction<R> of(java.util.function.LongFunction<? extends T> before, java.util.function.Function<? super T, ? extends R> after) {
		return new LongFunction<>() {
			@Override
			public R apply(long value) {
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
	static <R> LongFunction<R> of(java.util.function.LongPredicate before, BooleanFunction<? extends R> after) {
		return new LongFunction<>() {
			@Override
			public R apply(long value) {
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
	static <R> LongFunction<R> of(java.util.function.LongToDoubleFunction before, java.util.function.DoubleFunction<? extends R> after) {
		return new LongFunction<>() {
			@Override
			public R apply(long value) {
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
	static <R> LongFunction<R> of(java.util.function.LongToIntFunction before, java.util.function.IntFunction<? extends R> after) {
		return new LongFunction<>() {
			@Override
			public R apply(long value) {
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
	static <R> LongFunction<R> of(java.util.function.LongUnaryOperator before, java.util.function.LongFunction<? extends R> after) {
		return new LongFunction<>() {
			@Override
			public R apply(long value) {
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
	static <R> LongFunction<R> when(java.util.function.LongPredicate predicate, java.util.function.LongFunction<? extends R> accepted, java.util.function.LongFunction<? extends R> rejected) {
		return new LongFunction<>() {
			@Override
			public R apply(long value) {
				java.util.function.LongFunction<? extends R> function = predicate.test(value) ? accepted : rejected;
				return function.apply(value);
			}
		};
	}

	/**
	 * A simple function returning a fixed value.
	 * @param <V> the fixed value
	 */
	class SimpleLongFunction<V> implements LongFunction<V> {
		static final LongFunction<?> NULL = new SimpleLongFunction<>(null);

		private final V value;

		SimpleLongFunction(V value) {
			this.value = value;
		}

		@Override
		public V apply(long value) {
			return this.value;
		}

		@Override
		public Function<Long, V> box() {
			return Function.of(this.value);
		}
	}
}
