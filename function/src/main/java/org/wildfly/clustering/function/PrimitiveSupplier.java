/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A supplier of a primitive value.
 * @author Paul Ferraro
 * @param <B> the boxed type
 */
interface PrimitiveSupplier<B> extends VoidOperation, ToPrimitiveOperation<B> {

	@Override
	Supplier<B> thenBox();
}
