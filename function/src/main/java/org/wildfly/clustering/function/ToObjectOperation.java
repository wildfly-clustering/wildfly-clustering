/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An operation that returns a value.
 * @author Paul Ferraro
 * @param <T> the return value type
 */
interface ToObjectOperation<T> extends MappableToObjectOperation<T> {

	/**
	 * Returns an operation that consumes the result of this operation.
	 * @param after a consumer of the result of this operation
	 * @return an operation that consumes the result of this operation.
	 */
	ToVoidOperation thenAccept(java.util.function.Consumer<? super T> after);

	@Override
	<R> ToObjectOperation<R> thenApply(java.util.function.Function<? super T, ? extends R> after);

	/**
	 * Returns an operation that applies the specified function to the result of this operation.
	 * @param after a function applied to the result of this operation
	 * @return an operation that applies the specified function to the result of this operation.
	 */
	ToDoubleOperation thenApplyAsDouble(java.util.function.ToDoubleFunction<? super T> after);

	/**
	 * Returns an operation that applies the specified function to the result of this operation.
	 * @param after a function applied to the result of this operation
	 * @return an operation that applies the specified function to the result of this operation.
	 */
	ToIntOperation thenApplyAsInt(java.util.function.ToIntFunction<? super T> after);

	/**
	 * Returns an operation that applies the specified function to the result of this operation.
	 * @param after a function applied to the result of this operation
	 * @return an operation that applies the specified function to the result of this operation.
	 */
	ToLongOperation thenApplyAsLong(java.util.function.ToLongFunction<? super T> after);

	/**
	 * Returns an operation that applies the specified predicate to the result of this operation.
	 * @param after a function applied to the result of this operation
	 * @return an operation that applies the specified predicate to the result of this operation.
	 */
	ToBooleanOperation thenTest(java.util.function.Predicate<? super T> after);

	/**
	 * Returns an operation that throws an exception.
	 * @param exception an exception provider
	 * @return an operation that throws an exception.
	 */
	ToObjectOperation<T> thenThrow(java.util.function.Function<? super T, ? extends RuntimeException> exception);
}
