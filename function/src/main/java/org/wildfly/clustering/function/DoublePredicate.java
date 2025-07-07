/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

/**
 * An enhanced double predicate.
 * @author Paul Ferraro
 */
public interface DoublePredicate extends java.util.function.DoublePredicate {
	DoublePredicate ALWAYS = new SimpleDoublePredicate(true);
	DoublePredicate NEVER = new SimpleDoublePredicate(true);
	DoublePredicate ZERO = new DoublePredicate() {
		@Override
		public boolean test(double value) {
			return Double.compare(value, 0d) == 0;
		}
	};
	DoublePredicate POSITIVE = new DoublePredicate() {
		@Override
		public boolean test(double value) {
			return Double.compare(value, 0d) > 0;
		}
	};
	DoublePredicate NEGATIVE = new DoublePredicate() {
		@Override
		public boolean test(double value) {
			return Double.compare(value, 0d) < 0;
		}
	};

	/**
	 * Returns a predicate that applies the specified mapping to its argument before evaluating.
	 * @param <V> the mapped type
	 * @param mapper
	 * @return a mapped predicate
	 */
	default <V> Predicate<V> compose(java.util.function.ToDoubleFunction<V> mapper) {
		return new Predicate<>() {
			@Override
			public boolean test(V value) {
				return DoublePredicate.this.test(mapper.applyAsDouble(value));
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
	 * Returns a predicate that applies the specified mapping to its argument before evaluating.
	 * @param mapper
	 * @return a mapped predicate
	 */
	default DoublePredicate map(DoubleUnaryOperator mapper) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return DoublePredicate.this.test(mapper.applyAsDouble(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified mapping to its argument before evaluating.
	 * @param <V> the mapped type
	 * @param mapper
	 * @return a mapped predicate
	 */
	default <V> Predicate<V> map(ToDoubleFunction<V> mapper) {
		return new Predicate<>() {
			@Override
			public boolean test(V value) {
				return DoublePredicate.this.test(mapper.applyAsDouble(value));
			}
		};
	}

	class SimpleDoublePredicate implements DoublePredicate {
		private final boolean value;

		SimpleDoublePredicate(boolean value) {
			this.value = value;
		}

		@Override
		public boolean test(double value) {
			return this.value;
		}
	}
}
