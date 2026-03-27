/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An operation returning an int value.
 * @author Paul Ferraro
 */
interface ToIntOperation extends ToPrimitiveOperation<Integer> {

	/**
	 * Returns an operation that accepts the of this operation using the specified consumer.
	 * @param after a consumer that accepts the result of this operation
	 * @return an operation that accepts the of this operation using the specified consumer.
	 */
	ToVoidOperation thenAccept(java.util.function.IntConsumer after);

	/**
	 * Returns an operation that return the result of applying the specified function to the result of this operation.
	 * @param <R> the return type
	 * @param after a function applied to the result of this operation
	 * @return an operation that return the result of applying the specified function to the result of this operation.
	 */
	<R> ToObjectOperation<R> thenApply(java.util.function.IntFunction<? extends R> after);

	/**
	 * Returns an operation that return the result of applying the specified function to the result of this operation.
	 * @param after a function applied to the result of this operation
	 * @return an operation that return the result of applying the specified function to the result of this operation.
	 */
	ToDoubleOperation thenApplyAsDouble(java.util.function.IntToDoubleFunction after);

	/**
	 * Returns an operation that return the result of applying the specified function to the result of this operation.
	 * @param after a function applied to the result of this operation
	 * @return an operation that return the result of applying the specified function to the result of this operation.
	 */
	ToIntOperation thenApplyAsInt(java.util.function.IntUnaryOperator after);

	/**
	 * Returns an operation that return the result of applying the specified function to the result of this operation.
	 * @param after a function applied to the result of this operation
	 * @return an operation that return the result of applying the specified function to the result of this operation.
	 */
	ToLongOperation thenApplyAsLong(java.util.function.IntToLongFunction after);

	/**
	 * Returns an operation that return the result of applying the specified predicate to the result of this operation.
	 * @param after a predicate that tests the result of this operation
	 * @return an operation that return the result of applying the specified predicate to the result of this operation.
	 */
	ToBooleanOperation thenTest(java.util.function.IntPredicate after);
}
