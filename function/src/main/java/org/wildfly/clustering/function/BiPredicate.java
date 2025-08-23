/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced binary predicate.
 * @author Paul Ferraro
 * @param <T> the first parameter type
 * @param <U> the second parameter type
 */
public interface BiPredicate<T, U> extends java.util.function.BiPredicate<T, U> {
	BiPredicate<?, ?> ALWAYS = (value1, value2) -> true;
	BiPredicate<?, ?> NEVER = (value1, value2) -> false;

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
	 * Returns a predicate that always accepts its arguments.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
	 * @return a predicate that always accepts its arguments.
	 */
	@SuppressWarnings("unchecked")
	static <T, U> BiPredicate<T, U> always() {
		return (BiPredicate<T, U>) ALWAYS;
	}

	/**
	 * Returns a predicate that never accepts its arguments.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
	 * @return a predicate that never accepts its arguments.
	 */
	@SuppressWarnings("unchecked")
	static <T, U> BiPredicate<T, U> never() {
		return (BiPredicate<T, U>) NEVER;
	}

	/**
	 * Returns a binary predicate from a predicate that tests the first parameter only.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
	 * @param predicate the predicate for the first parameter
	 * @return a binary predicate from a predicate that tests the first parameter only.
	 */
	static <T, U> BiPredicate<T, U> former(java.util.function.Predicate<T> predicate) {
		return and(predicate, Predicate.always());
	}

	/**
	 * Returns a binary predicate from a predicate that tests the second parameter only.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
	 * @param predicate the predicate for the first parameter
	 * @return a binary predicate from a predicate that tests the first parameter only.
	 */
	static <T, U> BiPredicate<T, U> latter(java.util.function.Predicate<U> predicate) {
		return and(Predicate.always(), predicate);
	}

	/**
	 * Returns a binary predicate composed using the conjunction of two unary predicates.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
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
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
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
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
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
