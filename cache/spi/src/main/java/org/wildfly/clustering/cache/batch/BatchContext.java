/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.batch;

/**
 * Handles batch context switching.
 * @author Paul Ferraro
 */
public interface BatchContext extends AutoCloseable {
	@Override
	void close();
}
