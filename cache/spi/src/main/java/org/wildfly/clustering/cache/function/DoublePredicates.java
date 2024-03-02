/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.function.DoublePredicate;

/**
 * @author Paul Ferraro
 */
public enum DoublePredicates implements DoublePredicate {

	ALWAYS(value -> true),
	NEVER(value -> false),
	ZERO(value -> value == 0d),
	POSITIVE(value -> value > 0d),
	NEGATIVE(value -> value < 0d),
	;
	private DoublePredicate predicate;

	DoublePredicates(DoublePredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean test(double value) {
		return this.predicate.test(value);
	}
}
