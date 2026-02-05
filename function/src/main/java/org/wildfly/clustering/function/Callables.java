/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Shared implementations.
 * @author Paul Ferraro
 */
enum Callables implements Callable<Object> {
	NULL() {
		@Override
		public Object call() {
			return null;
		}
	},
	;

	@SuppressWarnings("unchecked")
	<T> Callable<T> cast() {
		return (Callable<T>) this;
	}
}
