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
	 * Returns a operation that accepts the of this operation using the specified consumer.
	 * @param consumer a consumer that accepts the result of this operation
	 * @return a operation that accepts the of this operation using the specified consumer.
	 */
	ToVoidOperation thenAccept(java.util.function.IntConsumer after);

	/**
	 * Returns a operation that return the result of applying the specified function to the result of this operation.
	 * @param function a function applied to the result of this operation
	 * @return a operation that return the result of applying the specified function to the result of this operation.
	 */
	<R> ToObjectOperation<R> thenApply(java.util.function.IntFunction<? extends R> after);

	/**
	 * Returns a operation that return the result of applying the specified function to the result of this operation.
	 * @param function a function applied to the result of this operation
	 * @return a operation that return the result of applying the specified function to the result of this operation.
	 */
	ToDoubleOperation thenApplyAsDouble(java.util.function.IntToDoubleFunction after);

	/**
	 * Returns a operation that return the result of applying the specified function to the result of this operation.
	 * @param function a function applied to the result of this operation
	 * @return a operation that return the result of applying the specified function to the result of this operation.
	 */
	ToIntOperation thenApplyAsInt(java.util.function.IntUnaryOperator after);

	/**
	 * Returns a operation that return the result of applying the specified function to the result of this operation.
	 * @param function a function applied to the result of this operation
	 * @return a operation that return the result of applying the specified function to the result of this operation.
	 */
	ToLongOperation thenApplyAsLong(java.util.function.IntToLongFunction after);

	/**
	 * Returns a operation that return the result of applying the specified predicate to the result of this operation.
	 * @param predicate a predicate that tests the result of this operation
	 * @return a operation that return the result of applying the specified predicate to the result of this operation.
	 */
	ToBooleanOperation thenTest(java.util.function.IntPredicate after);
}
