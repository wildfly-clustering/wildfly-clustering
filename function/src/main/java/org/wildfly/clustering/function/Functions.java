/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Shared implementations.
 * @author Paul Ferraro
 */
enum Functions implements Function<Object, Object> {
	NULL() {
		@Override
		public Object apply(Object value) {
			return null;
		}
	},
	IDENTITY() {
		@Override
		public Object apply(Object value) {
			return value;
		}
	},
	;

	@SuppressWarnings("unchecked")
	<T, R> Function<T, R> cast() {
		return (Function<T, R>) this;
	}
}
