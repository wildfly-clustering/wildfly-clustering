/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An operation returning a boolean value.
 * @author Paul Ferraro
 */
interface ToBooleanOperation extends ToPrimitiveOperation<Boolean> {

	/**
	 * Returns a operation that accepts the of this operation using the specified consumer.
	 * @param consumer a consumer that accepts the result of this operation
	 * @return a operation that accepts the of this operation using the specified consumer.
	 */
	ToVoidOperation thenAccept(BooleanConsumer after);

	/**
	 * Returns a operation that return the result of applying the specified function to the result of this operation.
	 * @param function a function applied to the result of this operation
	 * @return a operation that return the result of applying the specified function to the result of this operation.
	 */
	<R> ToObjectOperation<R> thenApply(BooleanFunction<? extends R> after);

	/**
	 * Returns a operation that return the result of applying the specified function to the result of this operation.
	 * @param function a function applied to the result of this operation
	 * @return a operation that return the result of applying the specified function to the result of this operation.
	 */
	ToDoubleOperation thenApplyAsDouble(BooleanToDoubleFunction after);

	/**
	 * Returns a operation that return the result of applying the specified function to the result of this operation.
	 * @param function a function applied to the result of this operation
	 * @return a operation that return the result of applying the specified function to the result of this operation.
	 */
	ToIntOperation thenApplyAsInt(BooleanToIntFunction after);

	/**
	 * Returns a operation that return the result of applying the specified function to the result of this operation.
	 * @param function a function applied to the result of this operation
	 * @return a operation that return the result of applying the specified function to the result of this operation.
	 */
	ToLongOperation thenApplyAsLong(BooleanToLongFunction after);

	/**
	 * Returns a operation that return the result of applying the specified predicate to the result of this operation.
	 * @param predicate a predicate that tests the result of this operation
	 * @return a operation that return the result of applying the specified predicate to the result of this operation.
	 */
	ToBooleanOperation thenTest(BooleanPredicate after);
}
