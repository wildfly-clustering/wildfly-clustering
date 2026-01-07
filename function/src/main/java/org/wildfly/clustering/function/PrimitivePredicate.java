/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A primtive predicate.
 * @author Paul Ferraro
 * @param <B> the boxed type
 * @param <P> the associated predicate type
 */
interface PrimitivePredicate<B, P> extends PrimitiveOperation<B>, ToBooleanOperation {

	@Override
	Predicate<B> box();

	/**
	 * Returns an operation that returns the negation of this operation.
	 * @return an operation that returns the negation of this operation.
	 */
	PrimitivePredicate<B, P> negate();

	/**
	 * Returns an operation that returns the conjunction of this and the specified operation.
	 * @return an operation that returns the conjunction of this and the specified operation.
	 */
	PrimitivePredicate<B, P> and(P other);

	/**
	 * Returns an operation that returns the disjunction of this and the specified operation.
	 * @return an operation that returns the disjunction of this and the specified operation.
	 */
	PrimitivePredicate<B, P> or(P other);
}
