/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.function.LongPredicate;

/**
 * @author Paul Ferraro
 */
public enum LongPredicates implements LongPredicate {

	ALWAYS(value -> true),
	NEVER(value -> false),
	ZERO(value -> value == 0L),
	POSITIVE(value -> value > 0L),
	NEGATIVE(value -> value < 0L),
	;
	private LongPredicate predicate;

	LongPredicates(LongPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean test(long value) {
		return this.predicate.test(value);
	}
}
