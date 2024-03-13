/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

/**
 * Reusable function implementations.
 * @author Paul Ferraro
 */
public class Functions {

	private Functions() {
		// Hide
	}

	private static final Function<?, ?> NULL_FUNCTION = constantFunction(null);

	/**
	 * Returns a function that always returns a constant result, regardless of input.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @return a function that always returns the specified result
	 */
	@SuppressWarnings("unchecked")
	public static <T, R> Function<T, R> nullFunction() {
		return (Function<T, R>) NULL_FUNCTION;
	}

	/**
	 * Returns a function that always returns a constant result, regardless of input.
	 * @param result the value to return by the constant function
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @return a function that always returns the specified result
	 */
	public static <T, R> Function<T, R> constantFunction(R result) {
		return new ConstantFunction<>(result);
	}

	/**
	 * Returns a function that always returns a constant result, regardless of input.
	 * @param result the value to return by the constant function
	 * @param <R> the function return type
	 * @return a function that always returns the specified result
	 */
	public static <R> IntFunction<R> constantIntFunction(R result) {
		return new ConstantFunction<>(result);
	}

	/**
	 * Returns a function that always returns a constant result, regardless of input.
	 * @param result the value to return by the constant function
	 * @param <R> the function return type
	 * @return a function that always returns the specified result
	 */
	public static <R> LongFunction<R> constantLongFunction(R result) {
		return new ConstantFunction<>(result);
	}

	/**
	 * Returns a function that always returns a constant result, regardless of input.
	 * @param result the value to return by the constant function
	 * @param <R> the function return type
	 * @return a function that always returns the specified result
	 */
	public static <R> DoubleFunction<R> constantDoubleFunction(R result) {
		return new ConstantFunction<>(result);
	}

	static class ConstantFunction<T, R> implements Function<T, R>, IntFunction<R>, LongFunction<R>, DoubleFunction<R> {
		private final R result;

		ConstantFunction(R result) {
			this.result = result;
		}

		@Override
		public R apply(T ignored) {
			return this.result;
		}

		@Override
		public R apply(double value) {
			return this.result;
		}

		@Override
		public R apply(long value) {
			return this.result;
		}

		@Override
		public R apply(int value) {
			return this.result;
		}
	}
}
