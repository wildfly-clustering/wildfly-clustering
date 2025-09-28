/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;

/**
 * Encapsulates the generic configuration of a cache.
 * @author Paul Ferraro
 */
public interface CacheConfiguration {

	/**
	 * Returns the properties of the associated cache.
	 * @return the properties of the associated cache.
	 */
	CacheProperties getCacheProperties();

	/**
	 * Returns a batch factory for the associated cache.
	 * @return a batch factory for the associated cache.
	 */
	Supplier<Batch> getBatchFactory();

	/**
	 * Indicates whether the associated cache is active.
	 * @return true, if the associated cache is active, false otherwise.
	 */
	default boolean isActive() {
		return true;
	}
}
