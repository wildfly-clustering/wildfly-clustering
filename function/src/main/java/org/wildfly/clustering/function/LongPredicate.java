/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced long predicate.
 * @author Paul Ferraro
 */
public interface LongPredicate extends java.util.function.LongPredicate {
	LongPredicate ALWAYS = value -> true;
	LongPredicate NEVER = value -> false;
	LongPredicate POSITIVE = greaterThan(0L);
	LongPredicate ZERO = equalTo(0L);
	LongPredicate NEGATIVE = lessThan(0L);

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param <V> the mapped type
	 * @param function a mapping function
	 * @return a mapped predicate
	 */
	default <V> Predicate<V> compose(java.util.function.ToLongFunction<V> function) {
		return new Predicate<>() {
			@Override
			public boolean test(V value) {
				return LongPredicate.this.test(function.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping function
	 * @return a mapped predicate
	 */
	default DoublePredicate composeDouble(java.util.function.DoubleToLongFunction function) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return LongPredicate.this.test(function.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping function
	 * @return a mapped predicate
	 */
	default IntPredicate composeInt(java.util.function.IntToLongFunction function) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return LongPredicate.this.test(function.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping function
	 * @return a mapped predicate
	 */
	default LongPredicate composeLong(java.util.function.LongUnaryOperator function) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return LongPredicate.this.test(function.applyAsLong(value));
			}
		};
	}

	@Override
	default LongPredicate and(java.util.function.LongPredicate other) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return LongPredicate.this.test(value) && other.test(value);
			}
		};
	}

	@Override
	default LongPredicate negate() {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return !LongPredicate.this.test(value);
			}
		};
	}

	@Override
	default LongPredicate or(java.util.function.LongPredicate other) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return LongPredicate.this.test(value) || other.test(value);
			}
		};
	}

	default LongPredicate xor(java.util.function.LongPredicate other) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return LongPredicate.this.test(value) ^ other.test(value);
			}
		};
	}

	/**
	 * Returns a predicate that always evaluates to the specified result.
	 * @param result the fixed result
	 * @return a predicate that always evaluates to the specified value.
	 */
	static LongPredicate of(boolean result) {
		return result ? LongPredicate.ALWAYS : LongPredicate.NEVER;
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static LongPredicate lessThan(long base) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return Long.compare(value, base) < 0L;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static LongPredicate equalTo(long base) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return Long.compare(base, value) == 0L;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static LongPredicate greaterThan(long base) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return Long.compare(value, base) > 0L;
			}
		};
	}
}
