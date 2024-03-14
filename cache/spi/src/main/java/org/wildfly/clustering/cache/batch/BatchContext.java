/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.batch;

/**
 * Handles batch context switching.
 * @param <B> the batch type
 * @author Paul Ferraro
 */
public interface BatchContext<B extends Batch> extends AutoCloseable {
	/**
	 * Returns the batch associated with this batch context.
	 * @return the batch associated with this batch context.
	 */
	B getBatch();

	@Override
	void close();
}
