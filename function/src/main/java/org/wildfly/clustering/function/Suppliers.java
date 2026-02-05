/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Shared implementations.
 * @author Paul Ferraro
 */
enum Suppliers implements Supplier<Object> {
	NULL() {
		@Override
		public Object get() {
			return null;
		}
	},
	;

	@SuppressWarnings("unchecked")
	<T> Supplier<T> cast() {
		return (Supplier<T>) this;
	}
}
