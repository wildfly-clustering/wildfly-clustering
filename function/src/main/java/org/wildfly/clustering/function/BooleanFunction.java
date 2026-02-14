/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function with a boolean parameter.
 * @author Paul Ferraro
 * @param <V> the function return type
 */
public interface BooleanFunction<V> extends BooleanOperation, PrimitiveFunction<Boolean, V> {
	/**
	 * Applies the specified value.
	 * @param value the function parameter
	 * @return the function result
	 */
	V apply(boolean value);

	@Override
	default Function<Boolean, V> box() {
		return this.compose(BooleanPredicate.identity().box());
	}

	@Override
	default <T> Function<T, V> compose(java.util.function.Predicate<? super T> before) {
		return Function.of(before, this);
	}

	@Override
	default <T1, T2> BiFunction<T1, T2, V> composeBinary(java.util.function.BiPredicate<? super T1, ? super T2> before) {
		return BiFunction.of(before, this);
	}

	@Override
	default BooleanFunction<V> composeBoolean(BooleanPredicate before) {
		return BooleanFunction.of(before, this);
	}

	@Override
	default DoubleFunction<V> composeDouble(java.util.function.DoublePredicate before) {
		return DoubleFunction.of(before, this);
	}

	@Override
	default IntFunction<V> composeInt(java.util.function.IntPredicate before) {
		return IntFunction.of(before, this);
	}

	@Override
	default LongFunction<V> composeLong(java.util.function.LongPredicate before) {
		return LongFunction.of(before, this);
	}
	@Override
	default BooleanConsumer thenAccept(java.util.function.Consumer<? super V> after) {
		return BooleanConsumer.of(this, after);
	}

	@Override
	default <R> BooleanFunction<R> thenApply(java.util.function.Function<? super V, ? extends R> after) {
		return BooleanFunction.of(this, after);
	}

	@Override
	default BooleanToDoubleFunction thenApplyAsDouble(java.util.function.ToDoubleFunction<? super V> after) {
		return BooleanToDoubleFunction.of(this, after);
	}

	@Override
	default BooleanToIntFunction thenApplyAsInt(java.util.function.ToIntFunction<? super V> after) {
		return BooleanToIntFunction.of(this, after);
	}

	@Override
	default BooleanToLongFunction thenApplyAsLong(java.util.function.ToLongFunction<? super V> after) {
		return BooleanToLongFunction.of(this, after);
	}

	@Override
	default BooleanPredicate thenTest(java.util.function.Predicate<? super V> after) {
		return BooleanPredicate.of(this, after);
	}

	@Override
	default BooleanFunction<V> thenThrow(java.util.function.Function<? super V, ? extends RuntimeException> exception) {
		return new BooleanFunction<>() {
			@Override
			public V apply(boolean value) {
				throw exception.apply(BooleanFunction.this.apply(value));
			}
		};
	}

	/**
	 * Returns a function that always returns the specified value, ignoring its parameter.
	 * @param <R> the function return type
	 * @param result the function return value
	 * @return a function that always returns the specified value, ignoring its parameter.
	 */
	static <R> BooleanFunction<R> of(R result) {
		return new BooleanFunction<>() {
			@Override
			public R apply(boolean value) {
				return result;
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
	static <R> BooleanFunction<R> of(BooleanPredicate before, BooleanFunction<? extends R> after) {
		return new BooleanFunction<>() {
			@Override
			public R apply(boolean value) {
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
	static <R> BooleanFunction<R> of(BooleanConsumer before, java.util.function.Supplier<? extends R> after) {
		return new BooleanFunction<>() {
			@Override
			public R apply(boolean value) {
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
	static <T, R> BooleanFunction<R> of(BooleanFunction<? extends T> before, java.util.function.Function<? super T, ? extends R> after) {
		return new BooleanFunction<>() {
			@Override
			public R apply(boolean value) {
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
	static <R> BooleanFunction<R> of(BooleanToDoubleFunction before, java.util.function.DoubleFunction<? extends R> after) {
		return new BooleanFunction<>() {
			@Override
			public R apply(boolean value) {
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
	static <R> BooleanFunction<R> of(BooleanToIntFunction before, java.util.function.IntFunction<? extends R> after) {
		return new BooleanFunction<>() {
			@Override
			public R apply(boolean value) {
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
	static <R> BooleanFunction<R> of(BooleanToLongFunction before, java.util.function.LongFunction<? extends R> after) {
		return new BooleanFunction<>() {
			@Override
			public R apply(boolean value) {
				return after.apply(before.applyAsLong(value));
			}
		};
	}

	/**
	 * A simple function returning a fixed value.
	 * @param <V> the fixed value
	 */
	class SimpleBooleanFunction<V> implements BooleanFunction<V> {
		static final BooleanFunction<?> NULL = new SimpleBooleanFunction<>(null);

		private final V value;

		SimpleBooleanFunction(V value) {
			this.value = value;
		}

		@Override
		public V apply(boolean value) {
			return this.value;
		}

		@Override
		public Function<Boolean, V> box() {
			return Function.of(this.value);
		}
	}
}
