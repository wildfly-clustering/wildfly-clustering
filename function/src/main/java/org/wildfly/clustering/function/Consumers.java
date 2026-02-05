/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Shared implementations.
 * @author Paul Ferraro
 */
enum Consumers implements Consumer<Object> {
	EMPTY() {
		@Override
		public void accept(Object ignore) {
			// Do nothing
		}
	},
	;

	@SuppressWarnings("unchecked")
	<T> Consumer<T> cast() {
		return (Consumer<T>) this;
	}
}
