/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.function.IntPredicate;

/**
 * @author Paul Ferraro
 */
public enum IntPredicates implements IntPredicate {

	ALWAYS(value -> true),
	NEVER(value -> false),
	ZERO(value -> value == 0),
	POSITIVE(value -> value > 0),
	NEGATIVE(value -> value < 0),
	;
	private IntPredicate predicate;

	IntPredicates(IntPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean test(int value) {
		return this.predicate.test(value);
	}
}
