/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Shared implementations.
 * @author Paul Ferraro
 */
enum BiPredicates implements BiPredicate<Object, Object> {
	NEVER() {
		@Override
		public boolean test(Object value1, Object value2) {
			return false;
		}
	},
	ALWAYS() {
		@Override
		public boolean test(Object value1, Object value2) {
			return true;
		}
	},
	;

	@SuppressWarnings("unchecked")
	<T1, T2> BiPredicate<T1, T2> cast() {
		return (BiPredicate<T1, T2>) this;
	}
}
