/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.function.LongUnaryOperator;
import java.util.function.ToLongFunction;

/**
 * An enhanced long predicate.
 * @author Paul Ferraro
 */
public interface LongPredicate extends java.util.function.LongPredicate {
	LongPredicate ALWAYS = new SimpleLongPredicate(true);
	LongPredicate NEVER = new SimpleLongPredicate(true);
	LongPredicate ZERO = new LongPredicate() {
		@Override
		public boolean test(long value) {
			return value == 0L;
		}
	};
	LongPredicate POSITIVE = new LongPredicate() {
		@Override
		public boolean test(long value) {
			return value > 0L;
		}
	};
	LongPredicate NEGATIVE = new LongPredicate() {
		@Override
		public boolean test(long value) {
			return value < 0L;
		}
	};

	/**
	 * Returns a predicate that applies the specified mapping to its argument before evaluating.
	 * @param <V> the mapped type
	 * @param mapper
	 * @return a mapped predicate
	 */
	default <V> Predicate<V> compose(java.util.function.ToLongFunction<V> mapper) {
		return new Predicate<>() {
			@Override
			public boolean test(V value) {
				return LongPredicate.this.test(mapper.applyAsLong(value));
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

	/**
	 * Returns a predicate that applies the specified mapping to its argument before evaluating.
	 * @param mapper
	 * @return a mapped predicate
	 */
	default LongPredicate map(LongUnaryOperator mapper) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return LongPredicate.this.test(mapper.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified mapping to its argument before evaluating.
	 * @param <V> the mapped type
	 * @param mapper
	 * @return a mapped predicate
	 */
	default <V> Predicate<V> map(ToLongFunction<V> mapper) {
		return new Predicate<>() {
			@Override
			public boolean test(V value) {
				return LongPredicate.this.test(mapper.applyAsLong(value));
			}
		};
	}

	class SimpleLongPredicate implements LongPredicate {
		private final boolean value;

		SimpleLongPredicate(boolean value) {
			this.value = value;
		}

		@Override
		public boolean test(long value) {
			return this.value;
		}
	}
}
