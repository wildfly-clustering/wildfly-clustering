/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * A {@link java.util.function.Predicate} extension that supports mapping.
 * @author Paul Ferraro
 * @param <T> the argument type
 */
public interface Predicate<T> extends java.util.function.Predicate<T> {
	Predicate<?> ALWAYS = new SimplePredicate<>(true);
	Predicate<?> NEVER = new SimplePredicate<>(false);

	/**
	 * Returns a predicate that always accepts its argument.
	 * @param <T> the argument type
	 * @return a predicate that always accepts its argument.
	 */
	@SuppressWarnings("unchecked")
	static <T> Predicate<T> always() {
		return (Predicate<T>) ALWAYS;
	}

	/**
	 * Returns a predicate that never accepts its argument.
	 * @param <T> the argument type
	 * @return a predicate that never accepts its argument.
	 */
	@SuppressWarnings("unchecked")
	static <T> Predicate<T> never() {
		return (Predicate<T>) NEVER;
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param object the object whose reference must match the predicate argument
	 * @param <T> the argument type
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static <T> Predicate<T> isEqual(T object) {
		return (object == null) ? Objects::isNull : object::equals;
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument references the specified object.
	 * @param object the object whose reference must match the predicate argument
	 * @param <T> the argument type
	 * @return a predicate that evaluates to true if and only if the argument references the specified object.
	 */
	static <T> Predicate<T> same(T object) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return object == value;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to the negation of the specified predicate.
	 * @param predicate the predicate to negate
	 * @param <T> the argument type
	 * @return a predicate that evaluates to the negation of the specified predicate.
	 */
	@SuppressWarnings("unchecked")
	static <T> Predicate<T> not(Predicate<? super T> predicate) {
		return (Predicate<T>) predicate.negate();
	}

	/**
	 * Returns a predicate that applies the specified mapping to its argument before evaluating.
	 * @param <V> the mapped type
	 * @param mapper
	 * @return a mapped predicate
	 */
	default <V> Predicate<V> map(Function<V, T> mapper) {
		return new Predicate<>() {
			@Override
			public boolean test(V test) {
				return Predicate.this.test(mapper.apply(test));
			}
		};
	}

	@Override
	default Predicate<T> and(java.util.function.Predicate<? super T> other) {
		return new Predicate<>() {
			@Override
			public boolean test(T test) {
				return Predicate.this.test(test) && other.test(test);
			}
		};
	}

	@Override
	default Predicate<T> negate() {
		return new Predicate<>() {
			@Override
			public boolean test(T test) {
				return !Predicate.this.test(test);
			}
		};
	}

	@Override
	default Predicate<T> or(java.util.function.Predicate<? super T> other) {
		return new Predicate<>() {
			@Override
			public boolean test(T test) {
				return Predicate.this.test(test) || other.test(test);
			}
		};
	}

	class SimplePredicate<T> implements Predicate<T> {
		private final boolean value;

		SimplePredicate(boolean value) {
			this.value = value;
		}

		@Override
		public boolean test(T value) {
			return this.value;
		}
	}
}
