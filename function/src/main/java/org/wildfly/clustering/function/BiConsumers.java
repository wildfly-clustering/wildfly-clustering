/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Shared implementations.
 * @author Paul Ferraro
 */
enum BiConsumers implements BiConsumer<Object, Object> {
	EMPTY() {
		@Override
		public void accept(Object object1, Object object2) {
			// Do nothing
		}
	},
	;

	@SuppressWarnings("unchecked")
	<T1, T2> BiConsumer<T1, T2> cast() {
		return (BiConsumer<T1, T2>) this;
	}
}
