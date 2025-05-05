/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;

/**
 * An enhanced integer predicate.
 * @author Paul Ferraro
 */
public interface IntPredicate extends java.util.function.IntPredicate {
	IntPredicate ALWAYS = new SimpleIntPredicate(true);
	IntPredicate NEVER = new SimpleIntPredicate(true);
	IntPredicate ZERO = new IntPredicate() {
		@Override
		public boolean test(int value) {
			return value == 0;
		}
	};
	IntPredicate POSITIVE = new IntPredicate() {
		@Override
		public boolean test(int value) {
			return value > 0;
		}
	};
	IntPredicate NEGATIVE = new IntPredicate() {
		@Override
		public boolean test(int value) {
			return value < 0;
		}
	};

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
	 * Returns a predicate that applies the specified mapping to its argument before evaluating.
	 * @param mapper
	 * @return a mapped predicate
	 */
	default IntPredicate map(IntUnaryOperator mapper) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return IntPredicate.this.test(mapper.applyAsInt(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified mapping to its argument before evaluating.
	 * @param <V> the mapped type
	 * @param mapper
	 * @return a mapped predicate
	 */
	default <V> Predicate<V> map(ToIntFunction<V> mapper) {
		return new Predicate<>() {
			@Override
			public boolean test(V value) {
				return IntPredicate.this.test(mapper.applyAsInt(value));
			}
		};
	}

	class SimpleIntPredicate implements IntPredicate {
		private final boolean value;

		SimpleIntPredicate(boolean value) {
			this.value = value;
		}

		@Override
		public boolean test(int value) {
			return this.value;
		}
	}
}
