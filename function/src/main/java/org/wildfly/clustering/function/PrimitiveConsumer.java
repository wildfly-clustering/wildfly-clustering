/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A consumer of a primitive value.
 * @author Paul Ferraro
 * @param <B> the boxed type
 * @param <P> the associated predicate type
 */
interface PrimitiveConsumer<B> extends PrimitiveOperation<B>, ToVoidOperation {
}
