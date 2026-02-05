/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Shared implementations.
 * @author Paul Ferraro
 */
enum BiFunctions implements BiFunction<Object, Object, Object> {
	NULL() {
		@Override
		public Object apply(Object object1, Object object2) {
			return null;
		}
	},
	FORMER() {
		@Override
		public Object apply(Object object1, Object object2) {
			return object1;
		}
	},
	LATTER() {
		@Override
		public Object apply(Object object1, Object object2) {
			return object2;
		}
	},
	;

	@SuppressWarnings("unchecked")
	<T1, T2, R> BiFunction<T1, T2, R> cast() {
		return (BiFunction<T1, T2, R>) this;
	}
}
