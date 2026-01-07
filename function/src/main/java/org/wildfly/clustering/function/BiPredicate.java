/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.Map;

/**
 * A predicate with two parameters.
 * @author Paul Ferraro
 * @param <V1> the former parameter type
 * @param <V2> the latter parameter type
 */
public interface BiPredicate<V1, V2> extends java.util.function.BiPredicate<V1, V2>, BinaryObjectOperation<V1, V2>, ToBooleanOperation {

	@Override
	default BiPredicate<V1, V2> and(java.util.function.BiPredicate<? super V1, ? super V2> other) {
		return BiPredicate.and(this, other);
	}

	@Override
	default BiPredicate<V1, V2> negate() {
		return BiPredicate.not(this);
	}

	@Override
	default BiPredicate<V1, V2> or(java.util.function.BiPredicate<? super V1, ? super V2> other) {
		return BiPredicate.or(this, other);
	}

	@Override
	default BiPredicate<V2, V1> reverse() {
		return new BiPredicate<>() {
			@Override
			public boolean test(V2 value2, V1 value1) {
				return BiPredicate.this.test(value1, value2);
			}
		};
	}

	@Override
	default <T1, T2> BiPredicate<T1, T2> compose(java.util.function.Function<? super T1, ? extends V1> before1, java.util.function.Function<? super T2, ? extends V2> before2) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T1 value1, T2 value2) {
				return BiPredicate.this.test(before1.apply(value1), before2.apply(value2));
			}
		};
	}

	@Override
	default Predicate<Map.Entry<V1, V2>> composeEntry() {
		return this.composeUnary(Map.Entry::getKey, Map.Entry::getValue);
	}

	@Override
	default <T> Predicate<T> composeUnary(java.util.function.Function<? super T, ? extends V1> before1, java.util.function.Function<? super T, ? extends V2> before2) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return BiPredicate.this.test(before1.apply(value), before2.apply(value));
			}
		};
	}

	@Override
	default BiConsumer<V1, V2> thenAccept(BooleanConsumer consumer) {
		return BiConsumer.of(this, consumer);
	}

	@Override
	default <R> BiFunction<V1, V2, R> thenApply(BooleanFunction<? extends R> after) {
		return BiFunction.of(this, after);
	}

	@Override
	default ToDoubleBiFunction<V1, V2> thenApplyAsDouble(BooleanToDoubleFunction after) {
		return ToDoubleBiFunction.of(this, after);
	}

	@Override
	default ToIntBiFunction<V1, V2> thenApplyAsInt(BooleanToIntFunction after) {
		return ToIntBiFunction.of(this, after);
	}

	@Override
	default ToLongBiFunction<V1, V2> thenApplyAsLong(BooleanToLongFunction after) {
		return ToLongBiFunction.of(this, after);
	}

	@Override
	default BiFunction<V1, V2, Boolean> thenBox() {
		return this.thenApply(BooleanPredicate.identity().thenBox());
	}

	@Override
	default BiPredicate<V1, V2> thenTest(BooleanPredicate after) {
		return BiPredicate.of(this, after);
	}

	/**
	 * Returns a predicate that always returns the specified value.
	 * @param <T> the former parameter type
	 * @param <U> the latter parameter type
	 * @param value the value to be returned by this predicate
	 * @return a predicate that always returns the specified value.
	 */
	@SuppressWarnings("unchecked")
	static <T, U> BiPredicate<T, U> of(boolean value) {
		return (BiPredicate<T, U>) (value ? SimpleBiPredicate.ALWAYS : SimpleBiPredicate.NEVER);
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former parameter
	 * @param after the latter parameter
	 * @return a composed predicate
	 */
	static <T1, T2> BiPredicate<T1, T2> of(java.util.function.BiConsumer<? super T1, ? super T2> before, java.util.function.BooleanSupplier after) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T1 value1, T2 value2) {
				before.accept(value1, value2);
				return after.getAsBoolean();
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former parameter
	 * @param after the latter parameter
	 * @return a composed predicate
	 */
	static <T1, T2> BiPredicate<T1, T2> of(java.util.function.BiPredicate<? super T1, ? super T2> before, BooleanPredicate after) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T1 value1, T2 value2) {
				return after.test(before.test(value1, value2));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param <V> the intermediate type
	 * @param before the former parameter
	 * @param after the latter parameter
	 * @return a composed predicate
	 */
	static <T1, T2, V> BiPredicate<T1, T2> of(java.util.function.BiFunction<? super T1, ? super T2, ? extends V> before, java.util.function.Predicate<? super V> after) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T1 value1, T2 value2) {
				return after.test(before.apply(value1, value2));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former parameter
	 * @param after the latter parameter
	 * @return a composed predicate
	 */
	static <T1, T2> BiPredicate<T1, T2> of(java.util.function.ToDoubleBiFunction<? super T1, ? super T2> before, java.util.function.DoublePredicate after) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T1 value1, T2 value2) {
				return after.test(before.applyAsDouble(value1, value2));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former parameter
	 * @param after the latter parameter
	 * @return a composed predicate
	 */
	static <T1, T2> BiPredicate<T1, T2> of(java.util.function.ToIntBiFunction<? super T1, ? super T2> before, java.util.function.IntPredicate after) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T1 value1, T2 value2) {
				return after.test(before.applyAsInt(value1, value2));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former parameter
	 * @param after the latter parameter
	 * @return a composed predicate
	 */
	static <T1, T2> BiPredicate<T1, T2> of(java.util.function.ToLongBiFunction<? super T1, ? super T2> before, java.util.function.LongPredicate after) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T1 value1, T2 value2) {
				return after.test(before.applyAsLong(value1, value2));
			}
		};
	}

	/**
	 * Returns a binary predicate from a predicate that tests the first parameter only.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param predicate the predicate for the first parameter
	 * @return a binary predicate from a predicate that tests the first parameter only.
	 */
	static <V1, V2> BiPredicate<V1, V2> former(java.util.function.Predicate<V1> predicate) {
		return and(predicate, Predicate.of(true));
	}

	/**
	 * Returns a binary predicate from a predicate that tests the second parameter only.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param predicate the predicate for the first parameter
	 * @return a binary predicate from a predicate that tests the first parameter only.
	 */
	static <V1, V2> BiPredicate<V1, V2> latter(java.util.function.Predicate<V2> predicate) {
		return and(Predicate.of(true), predicate);
	}

	/**
	 * Returns a predicate that evaluates to the inverse of the specified predicate.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param predicate the source predicate
	 * @return an inverse predicate
	 */
	static <V1, V2> BiPredicate<V1, V2> not(java.util.function.BiPredicate<? super V1, ? super V2> predicate) {
		return new BiPredicate<>() {
			@Override
			public boolean test(V1 value1, V2 value2) {
				return !predicate.test(value1, value2);
			}
		};
	}

	/**
	 * Returns a binary predicate composed using the conjunction of the specified predicates.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param predicate1 the predicate for the first parameter
	 * @param predicate2 the predicate for the second parameter
	 * @return a binary predicate composed using the conjunction of the specified predicates.
	 */
	static <V1, V2> BiPredicate<V1, V2> and(java.util.function.BiPredicate<? super V1, ? super V2> predicate1, java.util.function.BiPredicate<? super V1, ? super V2> predicate2) {
		return new BiPredicate<>() {
			@Override
			public boolean test(V1 value1, V2 value2) {
				return predicate1.test(value1, value2) && predicate2.test(value1, value2);
			}
		};
	}

	/**
	 * Returns a binary predicate composed using the conjunction of two unary predicates.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param predicate1 the predicate for the first parameter
	 * @param predicate2 the predicate for the second parameter
	 * @return a binary predicate composed using the conjunction of two unary predicates.
	 */
	static <V1, V2> BiPredicate<V1, V2> and(java.util.function.Predicate<V1> predicate1, java.util.function.Predicate<V2> predicate2) {
		return new BiPredicate<>() {
			@Override
			public boolean test(V1 value1, V2 value2) {
				return predicate1.test(value1) && predicate2.test(value2);
			}
		};
	}

	/**
	 * Returns a binary predicate composed using the disjunction of the specified predicates.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param predicate1 the predicate for the first parameter
	 * @param predicate2 the predicate for the second parameter
	 * @return a binary predicate composed using the disjunction of the specified predicates.
	 */
	static <V1, V2> BiPredicate<V1, V2> or(java.util.function.BiPredicate<? super V1, ? super V2> predicate1, java.util.function.BiPredicate<? super V1, ? super V2> predicate2) {
		return new BiPredicate<>() {
			@Override
			public boolean test(V1 value1, V2 value2) {
				return predicate1.test(value1, value2) || predicate2.test(value1, value2);
			}
		};
	}

	/**
	 * Returns a binary predicate composed using the disjunction of two unary predicates.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param predicate1 the predicate for the first parameter
	 * @param predicate2 the predicate for the second parameter
	 * @return a binary predicate composed using the disjunction of two unary predicates.
	 */
	static <V1, V2> BiPredicate<V1, V2> or(java.util.function.Predicate<V1> predicate1, java.util.function.Predicate<V2> predicate2) {
		return new BiPredicate<>() {
			@Override
			public boolean test(V1 value1, V2 value2) {
				return predicate1.test(value1) || predicate2.test(value2);
			}
		};
	}

	/**
	 * A predicate that returns a fixed value.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 */
	class SimpleBiPredicate<V1, V2> implements BiPredicate<V1, V2> {
		static final BiPredicate<?, ?> ALWAYS = new SimpleBiPredicate<>(true);
		static final BiPredicate<?, ?> NEVER = new SimpleBiPredicate<>(false);

		private final boolean result;

		private SimpleBiPredicate(boolean result) {
			this.result = result;
		}

		@Override
		public boolean test(V1 value1, V2 value2) {
			return this.result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public BiPredicate<V1, V2> and(java.util.function.BiPredicate<? super V1, ? super V2> other) {
			return this.result ? ((other instanceof BiPredicate<?, ?> predicate) ? (BiPredicate<V1, V2>) predicate : other::test) : (BiPredicate<V1, V2>) NEVER;
		}

		@SuppressWarnings("unchecked")
		@Override
		public BiPredicate<V1, V2> negate() {
			return (BiPredicate<V1, V2>) (this.result ? NEVER : ALWAYS);
		}

		@SuppressWarnings("unchecked")
		@Override
		public BiPredicate<V1, V2> or(java.util.function.BiPredicate<? super V1, ? super V2> other) {
			return this.result ? (BiPredicate<V1, V2>) ALWAYS : ((other instanceof BiPredicate<?, ?> predicate) ? (BiPredicate<V1, V2>) predicate : other::test);
		}

		@SuppressWarnings("unchecked")
		@Override
		public BiPredicate<V2, V1> reverse() {
			return (BiPredicate<V2, V1>) (this.result ? ALWAYS : NEVER);
		}
	}
}
