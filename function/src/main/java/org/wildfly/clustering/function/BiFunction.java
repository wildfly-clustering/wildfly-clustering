/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.Map;

/**
 * A function with two parameters.
 * @author Paul Ferraro
 * @param <V1> the former parameter type
 * @param <V2> the latter parameter type
 * @param <R> the result type
 */
public interface BiFunction<V1, V2, R> extends java.util.function.BiFunction<V1, V2, R>, BinaryObjectOperation<V1, V2>, ToObjectOperation<R> {

	@Override
	default <T1, T2> BiFunction<T1, T2, R> compose(java.util.function.Function<? super T1, ? extends V1> beforeFormer, java.util.function.Function<? super T2, ? extends V2> beforeLatter) {
		return new BiFunction<>() {
			@Override
			public R apply(T1 value1, T2 value2) {
				return BiFunction.this.apply(beforeFormer.apply(value1), beforeLatter.apply(value2));
			}
		};
	}

	@Override
	default Function<Map.Entry<V1, V2>, R> composeEntry() {
		return this.composeUnary(Map.Entry::getKey, Map.Entry::getValue);
	}

	@Override
	default <T> Function<T, R> composeUnary(java.util.function.Function<? super T, ? extends V1> beforeFormer, java.util.function.Function<? super T, ? extends V2> beforeLatter) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				return BiFunction.this.apply(beforeFormer.apply(value), beforeLatter.apply(value));
			}
		};
	}

	@Override
	default BiConsumer<V1, V2> thenAccept(java.util.function.Consumer<? super R> after) {
		return BiConsumer.of(this, after);
	}

	@Override
	default <RR> BiFunction<V1, V2, RR> thenApply(java.util.function.Function<? super R, ? extends RR> after) {
		return new BiFunction<>() {
			@Override
			public RR apply(V1 value1, V2 value2) {
				return after.apply(BiFunction.this.apply(value1, value2));
			}
		};
	}

	@Override
	default ToDoubleBiFunction<V1, V2> thenApplyAsDouble(java.util.function.ToDoubleFunction<? super R> after) {
		return ToDoubleBiFunction.of(this, after);
	}

	@Override
	default ToIntBiFunction<V1, V2> thenApplyAsInt(java.util.function.ToIntFunction<? super R> after) {
		return ToIntBiFunction.of(this, after);
	}

	@Override
	default ToLongBiFunction<V1, V2> thenApplyAsLong(java.util.function.ToLongFunction<? super R> after) {
		return ToLongBiFunction.of(this, after);
	}

	@Override
	default BiPredicate<V1, V2> thenTest(java.util.function.Predicate<? super R> after) {
		return BiPredicate.of(this, after);
	}

	@Override
	default <V> BiFunction<V1, V2, V> andThen(java.util.function.Function<? super R, ? extends V> after) {
		return BiFunction.of(this, after);
	}

	@Override
	default BiFunction<V1, V2, R> thenThrow(java.util.function.Function<? super R, ? extends RuntimeException> exception) {
		return new BiFunction<>() {
			@Override
			public R apply(V1 value1, V2 value2) {
				throw exception.apply(BiFunction.this.apply(value1, value2));
			}
		};
	}

	@Override
	default BiFunction<V2, V1, R> reverse() {
		return new BiFunction<>() {
			@Override
			public R apply(V2 value2, V1 value1) {
				return BiFunction.this.apply(value1, value2);
			}
		};
	}

	/**
	 * Returns a function that returns its first parameter.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
	 * @param <R> the function return type
	 * @return a function that returns its first parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T extends R, U, R> BiFunction<T, U, R> former() {
		return (BiFunction<T, U, R>) FormerBiFunction.INSTANCE;
	}

	/**
	 * Returns a function that returns its second parameter.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
	 * @param <R> the function return type
	 * @return a function that returns its first parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T, U extends R, R> BiFunction<T, U, R> latter() {
		return (BiFunction<T, U, R>) LatterBiFunction.INSTANCE;
	}

	/**
	 * Returns a function that always returns the specified value, ignoring its parameter.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
	 * @param <R> the function return type
	 * @param result the function result
	 * @return a function that always returns the specified value, ignoring its parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T, U, R> BiFunction<T, U, R> of(R result) {
		return (result != null) ? of(BiConsumer.of(), Supplier.of(result)) : (BiFunction<T, U, R>) SimpleBiFunction.NULL;
	}

	/**
	 * Returns a composite function that combines the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function that combines the specified operations.
	 */
	static <T1, T2, R> BiFunction<T1, T2, R> of(java.util.function.BiConsumer<? super T1, ? super T2> before, java.util.function.Supplier<? extends R> after) {
		return new BiFunction<>() {
			@Override
			public R apply(T1 value1, T2 value2) {
				before.accept(value1, value2);
				return after.get();
			}
		};
	}

	/**
	 * Returns a composite function that combines the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param <V> the intermediate type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function that combines the specified operations.
	 */
	static <T1, T2, V, R> BiFunction<T1, T2, R> of(java.util.function.BiFunction<? super T1, ? super T2, ? extends V> before, java.util.function.Function<? super V, ? extends R> after) {
		return new BiFunction<>() {
			@Override
			public R apply(T1 value1, T2 value2) {
				return after.apply(before.apply(value1, value2));
			}
		};
	}

	/**
	 * Returns a composite function that combines the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function that combines the specified operations.
	 */
	static <T1, T2, R> BiFunction<T1, T2, R> of(java.util.function.BiPredicate<? super T1, ? super T2> before, BooleanFunction<? extends R> after) {
		return new BiFunction<>() {
			@Override
			public R apply(T1 value1, T2 value2) {
				return after.apply(before.test(value1, value2));
			}
		};
	}

	/**
	 * Returns a composite function that combines the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function that combines the specified operations.
	 */
	static <T1, T2, R> BiFunction<T1, T2, R> of(java.util.function.ToDoubleBiFunction<? super T1, ? super T2> before, java.util.function.DoubleFunction<? extends R> after) {
		return new BiFunction<>() {
			@Override
			public R apply(T1 value1, T2 value2) {
				return after.apply(before.applyAsDouble(value1, value2));
			}
		};
	}

	/**
	 * Returns a composite function that combines the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function that combines the specified operations.
	 */
	static <T1, T2, R> BiFunction<T1, T2, R> of(java.util.function.ToIntBiFunction<? super T1, ? super T2> before, java.util.function.IntFunction<? extends R> after) {
		return new BiFunction<>() {
			@Override
			public R apply(T1 value1, T2 value2) {
				return after.apply(before.applyAsInt(value1, value2));
			}
		};
	}

	/**
	 * Returns a composite function that combines the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function that combines the specified operations.
	 */
	static <T1, T2, R> BiFunction<T1, T2, R> of(java.util.function.ToLongBiFunction<? super T1, ? super T2> before, java.util.function.LongFunction<? extends R> after) {
		return new BiFunction<>() {
			@Override
			public R apply(T1 value1, T2 value2) {
				return after.apply(before.applyAsLong(value1, value2));
			}
		};
	}

	/**
	 * Returns a function that returns the result of applying the specified function to its former parameter, ignoring its latter parameter.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param <R> the function return type
	 * @param function the function applied to the former parameter
	 * @return a function that returns the result of applying the specified function to its former parameter
	 */
	static <T1, T2, R> BiFunction<T1, T2, R> former(java.util.function.Function<? super T1, ? extends R> function) {
		return new FormerBiFunction<>(function);
	}

	/**
	 * Returns a function that returns the result of applying the specified function to its latter parameter, ignoring its former parameter.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param <R> the function return type
	 * @param function the function applied to the latter parameter
	 * @return a function that returns the result of applying the specified function to its latter parameter
	 */
	static <T1, T2, R> BiFunction<T1, T2, R> latter(java.util.function.Function<? super T2, ? extends R> function) {
		return new LatterBiFunction<>(function);
	}

	/**
	 * Returns a function that delegates to one of two functions based on the specified predicate.
	 * @param <T1> the former function parameter type
	 * @param <T2> the latter function parameter type
	 * @param <R> the function return type
	 * @param predicate a predicate
	 * @param accepted the function to apply when accepted by the specified predicate
	 * @param rejected the function to apply when rejected by the specified predicate
	 * @return a function that delegates to one of two functions based on the specified predicate.
	 */
	static <T1, T2, R> BiFunction<T1, T2, R> when(java.util.function.BiPredicate<? super T1, ? super T2> predicate, java.util.function.BiFunction<? super T1, ? super T2, ? extends R> accepted, java.util.function.BiFunction<? super T1, ? super T2, ? extends R> rejected) {
		return new BiFunction<>() {
			@Override
			public R apply(T1 value1, T2 value2) {
				java.util.function.BiFunction<? super T1, ? super T2, ? extends R> function = predicate.test(value1, value2) ? accepted : rejected;
				return function.apply(value1, value2);
			}
		};
	}

	/**
	 * A function that returns a fixed value, ignoring its parameters.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param <R> the function return type
	 */
	class SimpleBiFunction<V1, V2, R> implements BiFunction<V1, V2, R> {
		static final BiFunction<?, ?, ?> NULL = new SimpleBiFunction<>(null);

		private final R value;

		SimpleBiFunction(R value) {
			this.value = value;
		}

		@Override
		public R apply(V1 value1, V2 value2) {
			return this.value;
		}

		@Override
		public BiFunction<V2, V1, R> reverse() {
			return new SimpleBiFunction<>(this.value);
		}
	}

	/**
	 * A function that returns its former parameter.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param <R> the function return type
	 */
	class FormerBiFunction<V1, V2, R> implements BiFunction<V1, V2, R> {
		static final BiFunction<?, ?, ?> INSTANCE = new FormerBiFunction<>(Function.identity()) {
			@SuppressWarnings("unchecked")
			@Override
			public BiFunction<Object, Object, Object> reverse() {
				return (BiFunction<Object, Object, Object>) LatterBiFunction.INSTANCE;
			}
		};

		private final java.util.function.Function<? super V1, ? extends R> function;

		FormerBiFunction(java.util.function.Function<? super V1, ? extends R> function) {
			this.function = function;
		}

		@Override
		public R apply(V1 value1, V2 value2) {
			return this.function.apply(value1);
		}
	}

	/**
	 * A function that returns its latter parameter.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param <R> the function return type
	 */
	class LatterBiFunction<V1, V2, R> implements BiFunction<V1, V2, R> {
		static final BiFunction<?, ?, ?> INSTANCE = new LatterBiFunction<>(Function.identity()) {
			@SuppressWarnings("unchecked")
			@Override
			public BiFunction<Object, Object, Object> reverse() {
				return (BiFunction<Object, Object, Object>) FormerBiFunction.INSTANCE;
			}
		};

		private final java.util.function.Function<? super V2, ? extends R> function;

		LatterBiFunction(java.util.function.Function<? super V2, ? extends R> function) {
			this.function = function;
		}

		@Override
		public R apply(V1 value1, V2 value2) {
			return this.function.apply(value2);
		}
	}
}
