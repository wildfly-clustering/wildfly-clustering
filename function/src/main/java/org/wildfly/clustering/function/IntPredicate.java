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
	/** A predicate that always returns true */
	IntPredicate ALWAYS = of(IntConsumer.EMPTY, BooleanSupplier.TRUE);
	/** A predicate that always returns false */
	IntPredicate NEVER = of(IntConsumer.EMPTY, BooleanSupplier.FALSE);
	/** A predicate that returns true if the parameter is greater than zero. */
	IntPredicate POSITIVE = greaterThan(0);
	/** A predicate that returns true if the parameter is zero. */
	IntPredicate ZERO = equalTo(0);
	/** A predicate that returns true if the parameter is less than zero. */
	IntPredicate NEGATIVE = lessThan(0);

	/**
	 * Returns a predicate that applies the specified mapping to its argument before evaluating.
	 * @param <V> the mapped type
	 * @param composer a composing function
	 * @return a mapped predicate
	 */
	default <V> Predicate<V> compose(java.util.function.ToIntFunction<V> composer) {
		return new Predicate<>() {
			@Override
			public boolean test(V value) {
				return IntPredicate.this.test(composer.applyAsInt(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param composer a composing function
	 * @return a mapped predicate
	 */
	default DoublePredicate composeDouble(java.util.function.DoubleToIntFunction composer) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return IntPredicate.this.test(composer.applyAsInt(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param composer a composing operator
	 * @return a mapped predicate
	 */
	default IntPredicate composeInt(java.util.function.IntUnaryOperator composer) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return IntPredicate.this.test(composer.applyAsInt(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param composer a composing function
	 * @return a mapped predicate
	 */
	default LongPredicate composeLong(java.util.function.LongToIntFunction composer) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return IntPredicate.this.test(composer.applyAsInt(value));
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

	/**
	 * Returns a predicate returning the exclusive disjunction of this predicate with the specified predicate.
	 * @param other another predicate
	 * @return a predicate returning the exclusive disjunction of this predicate with the specified predicate.
	 */
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
	 * Returns a predicate that accepts its parameter via the specified consumer and returns the result of the specified supplier.
	 * @param consumer the predicate parameter consumer
	 * @param supplier the predicate result supplier
	 * @return a predicate that accepts its parameter via the specified consumer and returns the result of the specified supplier.
	 */
	static IntPredicate of(java.util.function.IntConsumer consumer, java.util.function.BooleanSupplier supplier) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				consumer.accept(value);
				return supplier.getAsBoolean();
			}
		};
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
