/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced double predicate.
 * @author Paul Ferraro
 */
public interface DoublePredicate extends java.util.function.DoublePredicate {
	/** A predicate that always returns true */
	DoublePredicate ALWAYS = value -> true;
	/** A predicate that always returns false */
	DoublePredicate NEVER = value -> false;
	/** A predicate that returns true if its parameter is greater than zero. */
	DoublePredicate POSITIVE = greaterThan(0d);
	/** A predicate that returns true if its parameter is zero. */
	DoublePredicate ZERO = equalTo(0d);
	/** A predicate that returns true if its parameter is less than zero. */
	DoublePredicate NEGATIVE = lessThan(0d);

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param <V> the mapped type
	 * @param function a mapping function
	 * @return a mapped predicate
	 */
	default <V> Predicate<V> compose(java.util.function.ToDoubleFunction<V> function) {
		return new Predicate<>() {
			@Override
			public boolean test(V value) {
				return DoublePredicate.this.test(function.applyAsDouble(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping operator
	 * @return a mapped predicate
	 */
	default DoublePredicate composeDouble(java.util.function.DoubleUnaryOperator function) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return DoublePredicate.this.test(function.applyAsDouble(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping function
	 * @return a mapped predicate
	 */
	default IntPredicate composeInt(java.util.function.IntToDoubleFunction function) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return DoublePredicate.this.test(function.applyAsDouble(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping function
	 * @return a mapped predicate
	 */
	default LongPredicate composeLong(java.util.function.LongToDoubleFunction function) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return DoublePredicate.this.test(function.applyAsDouble(value));
			}
		};
	}

	@Override
	default DoublePredicate and(java.util.function.DoublePredicate other) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return DoublePredicate.this.test(value) && other.test(value);
			}
		};
	}

	@Override
	default DoublePredicate negate() {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return !DoublePredicate.this.test(value);
			}
		};
	}

	@Override
	default DoublePredicate or(java.util.function.DoublePredicate other) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return DoublePredicate.this.test(value) || other.test(value);
			}
		};
	}

	/**
	 * Returns a predicate returning the exclusive disjunction of this predicate with the specified predicate.
	 * @param other another predicate
	 * @return a predicate returning the exclusive disjunction of this predicate with the specified predicate.
	 */
	default DoublePredicate xor(java.util.function.DoublePredicate other) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return DoublePredicate.this.test(value) ^ other.test(value);
			}
		};
	}

	/**
	 * Returns a predicate that always evaluates to the specified result.
	 * @param result the fixed result
	 * @return a predicate that always evaluates to the specified value.
	 */
	static DoublePredicate of(boolean result) {
		return result ? DoublePredicate.ALWAYS : DoublePredicate.NEVER;
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static DoublePredicate lessThan(double base) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return Double.compare(value, base) < 0;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static DoublePredicate equalTo(double base) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return Double.compare(base, value) == 0;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static DoublePredicate greaterThan(double base) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return Double.compare(value, base) > 0;
			}
		};
	}
}
