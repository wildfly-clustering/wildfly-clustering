/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced binary predicate.
 * @author Paul Ferraro
	 * @param <T> the former parameter type
	 * @param <U> the latter parameter type
 */
public interface BiPredicate<T, U> extends java.util.function.BiPredicate<T, U> {
	/** A predicate that always returns true */
	BiPredicate<?, ?> ALWAYS = of(BiConsumer.EMPTY, BooleanSupplier.TRUE);
	/** A predicate that always returns false */
	BiPredicate<?, ?> NEVER = of(BiConsumer.EMPTY, BooleanSupplier.FALSE);

	@Override
	default BiPredicate<T, U> and(java.util.function.BiPredicate<? super T, ? super U> other) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T value1, U value2) {
				return BiPredicate.this.test(value1, value2) && other.test(value1, value2);
			}
		};
	}

	@Override
	default BiPredicate<T, U> negate() {
		return new BiPredicate<>() {
			@Override
			public boolean test(T value1, U value2) {
				return !BiPredicate.this.test(value1, value2);
			}
		};
	}

	@Override
	default BiPredicate<T, U> or(java.util.function.BiPredicate<? super T, ? super U> other) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T value1, U value2) {
				return BiPredicate.this.test(value1, value2) || other.test(value1, value2);
			}
		};
	}

	/**
	 * Returns a composed predicate that represents a logical XOR of the specified predicate.
	 * @param other another predicate that should evaluate to the opposite of this one.
	 * @return a composed predicate that represents a logical XOR of the specified predicate.
	 */
	default BiPredicate<T, U> xor(java.util.function.BiPredicate<? super T, ? super U> other) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T value1, U value2) {
				return BiPredicate.this.test(value1, value2) ^ other.test(value1, value2);
			}
		};
	}

	/**
	 * Returns a predicate that processes this predicate with reversed parameter order.
	 * @return a predicate that processes this predicate with reversed parameter order.
	 */
	default BiPredicate<U, T> reverse() {
		return new BiPredicate<>() {
			@Override
			public boolean test(U value2, T value1) {
				return BiPredicate.this.test(value1, value2);
			}
		};
	}

	/**
	 * Composes a predicate that applies the specified functions to each parameter as inputs to this predicate.
	 * @param <V1> the first parameter function type
	 * @param <V2> the second parameter function type
	 * @param before1 the function applied to the first parameter
	 * @param before2 the function applied to the second parameter
	 * @return a predicate that applies the specified functions to each parameter as inputs to this predicate.
	 */
	default <V1, V2> BiPredicate<V1, V2> compose(java.util.function.Function<? super V1, ? extends T> before1, java.util.function.Function<? super V2, ? extends U> before2) {
		return new BiPredicate<>() {
			@Override
			public boolean test(V1 value1, V2 value2) {
				return BiPredicate.this.test(before1.apply(value1), before2.apply(value2));
			}
		};
	}

	/**
	 * Composes a unary predicate that applies the specified functions to its parameter as inputs to this predicate.
	 * @param <V> the parameter function type
	 * @param before1 the function applied to the first parameter
	 * @param before2 the function applied to the second parameter
	 * @return a unary predicate that applies the specified functions to its parameter as inputs to this predicate.
	 */
	default <V> Predicate<V> composeUnary(java.util.function.Function<? super V, ? extends T> before1, java.util.function.Function<? super V, ? extends U> before2) {
		return new Predicate<>() {
			@Override
			public boolean test(V value) {
				return BiPredicate.this.test(before1.apply(value), before2.apply(value));
			}
		};
	}

	/**
	 * Returns a predicate that always accepts its arguments.
	 * @param <T> the former parameter type
	 * @param <U> the latter parameter type
	 * @return a predicate that always accepts its arguments.
	 */
	@SuppressWarnings("unchecked")
	static <T, U> BiPredicate<T, U> always() {
		return (BiPredicate<T, U>) ALWAYS;
	}

	/**
	 * Returns a predicate that never accepts its arguments.
	 * @param <T> the former parameter type
	 * @param <U> the latter parameter type
	 * @return a predicate that never accepts its arguments.
	 */
	@SuppressWarnings("unchecked")
	static <T, U> BiPredicate<T, U> never() {
		return (BiPredicate<T, U>) NEVER;
	}

	/**
	 * Returns a predicate that always returns the specified value.
	 * @param <T> the former parameter type
	 * @param <U> the latter parameter type
	 * @param value the value to be returned by this predicate
	 * @return a predicate that always returns the specified value.
	 */
	static <T, U> BiPredicate<T, U> of(boolean value) {
		return value ? always() : never();
	}

	/**
	 * Returns a predicate that accepts its parameter via the specified consumer and returns the value returned by the specified supplier.
	 * @param <T> the former parameter type
	 * @param <U> the latter parameter type
	 * @param consumer the consumer of the predicate parameter
	 * @param supplier the supplier of the predicate result
	 * @return a predicate that accepts its parameter via the specified consumer and returns the value returned by the specified supplier.
	 */
	static <T, U> BiPredicate<T, U> of(java.util.function.BiConsumer<T, U> consumer, java.util.function.BooleanSupplier supplier) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T value1, U value2) {
				consumer.accept(value1, value2);
				return supplier.getAsBoolean();
			}
		};
	}

	/**
	 * Returns a binary predicate from a predicate that tests the first parameter only.
	 * @param <T> the former parameter type
	 * @param <U> the latter parameter type
	 * @param predicate the predicate for the first parameter
	 * @return a binary predicate from a predicate that tests the first parameter only.
	 */
	static <T, U> BiPredicate<T, U> testFormer(java.util.function.Predicate<T> predicate) {
		return and(predicate, Predicate.always());
	}

	/**
	 * Returns a binary predicate from a predicate that tests the second parameter only.
	 * @param <T> the former parameter type
	 * @param <U> the latter parameter type
	 * @param predicate the predicate for the first parameter
	 * @return a binary predicate from a predicate that tests the first parameter only.
	 */
	static <T, U> BiPredicate<T, U> testLatter(java.util.function.Predicate<U> predicate) {
		return and(Predicate.always(), predicate);
	}

	/**
	 * Returns a binary predicate composed using the conjunction of two unary predicates.
	 * @param <T> the former parameter type
	 * @param <U> the latter parameter type
	 * @param predicate1 the predicate for the first parameter
	 * @param predicate2 the predicate for the second parameter
	 * @return a binary predicate composed using the conjunction of two unary predicates.
	 */
	static <T, U> BiPredicate<T, U> and(java.util.function.Predicate<T> predicate1, java.util.function.Predicate<U> predicate2) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T value1, U value2) {
				return predicate1.test(value1) && predicate2.test(value2);
			}
		};
	}

	/**
	 * Returns a binary predicate composed using the disjunction of two unary predicates.
	 * @param <T> the former parameter type
	 * @param <U> the latter parameter type
	 * @param predicate1 the predicate for the first parameter
	 * @param predicate2 the predicate for the second parameter
	 * @return a binary predicate composed using the disjunction of two unary predicates.
	 */
	static <T, U> BiPredicate<T, U> or(java.util.function.Predicate<T> predicate1, java.util.function.Predicate<U> predicate2) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T value1, U value2) {
				return predicate1.test(value1) || predicate2.test(value2);
			}
		};
	}

	/**
	 * Returns a binary predicate composed using the exclusive disjunction of two unary predicates.
	 * @param <T> the former parameter type
	 * @param <U> the latter parameter type
	 * @param predicate1 the predicate for the first parameter
	 * @param predicate2 the predicate for the second parameter
	 * @return a binary predicate composed using the exclusive disjunction of two unary predicates.
	 */
	static <T, U> BiPredicate<T, U> xor(java.util.function.Predicate<T> predicate1, java.util.function.Predicate<U> predicate2) {
		return new BiPredicate<>() {
			@Override
			public boolean test(T value1, U value2) {
				return predicate1.test(value1) ^ predicate2.test(value2);
			}
		};
	}
}
