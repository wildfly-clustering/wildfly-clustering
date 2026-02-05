/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Shared implementations.
 * @author Paul Ferraro
 */
enum Predicates implements Predicate<Object> {
	NEVER() {
		@Override
		public boolean test(Object value) {
			return false;
		}
	},
	ALWAYS() {
		@Override
		public boolean test(Object value) {
			return true;
		}
	},
	;

	@SuppressWarnings("unchecked")
	<T> Predicate<T> cast() {
		return (Predicate<T>) this;
	}
}
