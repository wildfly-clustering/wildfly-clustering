/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Shared implementations.
 * @author Paul Ferraro
 */
enum UnaryOperators implements UnaryOperator<Object> {
	NULL() {
		@Override
		public Object apply(Object object) {
			return null;
		}
	},
	IDENTITY() {
		@Override
		public Object apply(Object object) {
			return object;
		}
	},
	;

	@SuppressWarnings("unchecked")
	<T> UnaryOperator<T> cast() {
		return (UnaryOperator<T>) this;
	}
}
