/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.batch;

import java.util.function.Supplier;

import org.wildfly.clustering.context.Context;

/**
 * Handles batch context switching.
 * @author Paul Ferraro
 * @param <B> the batch type of this context
 */
public interface BatchContext<B> extends Supplier<B>, Context {
}
