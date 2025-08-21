/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced integer predicate.
 * @author Paul Ferraro
 */
public interface IntPredicate extends java.util.function.IntPredicate {
	IntPredicate ALWAYS = value -> true;
	IntPredicate NEVER = value -> false;
	IntPredicate POSITIVE = greaterThan(0);
	IntPredicate ZERO = equalTo(0);
	IntPredicate NEGATIVE = lessThan(0);

	/**
	 * Returns a predicate that applies the specified mapping to its argument before evaluating.
	 * @param <V> the mapped type
	 * @param mapper
	 * @return a mapped predicate
	 */
	default <V> Predicate<V> compose(java.util.function.ToIntFunction<V> mapper) {
		return new Predicate<>() {
			@Override
			public boolean test(V value) {
				return IntPredicate.this.test(mapper.applyAsInt(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping function
	 * @return a mapped predicate
	 */
	default DoublePredicate composeDouble(java.util.function.DoubleToIntFunction function) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return IntPredicate.this.test(function.applyAsInt(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping operator
	 * @return a mapped predicate
	 */
	default IntPredicate composeInt(java.util.function.IntUnaryOperator function) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return IntPredicate.this.test(function.applyAsInt(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping function
	 * @return a mapped predicate
	 */
	default LongPredicate composeLong(java.util.function.LongToIntFunction function) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return IntPredicate.this.test(function.applyAsInt(value));
			}
		};
	}

	@Override
	default IntPredicate and(java.util.function.IntPredicate other) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return IntPredicate.this.test(value) && other.test(value);
			}
		};
	}

	@Override
	default IntPredicate negate() {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return !IntPredicate.this.test(value);
			}
		};
	}

	@Override
	default IntPredicate or(java.util.function.IntPredicate other) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return IntPredicate.this.test(value) || other.test(value);
			}
		};
	}

	default IntPredicate xor(java.util.function.IntPredicate other) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return IntPredicate.this.test(value) ^ other.test(value);
			}
		};
	}

	/**
	 * Returns a predicate that always evaluates to the specified result.
	 * @param result the fixed result
	 * @return a predicate that always evaluates to the specified value.
	 */
	static IntPredicate of(boolean result) {
		return result ? IntPredicate.ALWAYS : IntPredicate.NEVER;
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static IntPredicate lessThan(int base) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return Integer.compare(value, base) < 0;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static IntPredicate equalTo(int base) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return Integer.compare(base, value) == 0;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static IntPredicate greaterThan(int base) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return Integer.compare(value, base) > 0;
			}
		};
	}
}
