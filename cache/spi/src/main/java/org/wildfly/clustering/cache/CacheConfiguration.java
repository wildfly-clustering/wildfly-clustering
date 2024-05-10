/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import java.util.function.Supplier;

import org.wildfly.clustering.cache.batch.Batch;

/**
 * Encapsulates the generic configuration of a cache.
 * @author Paul Ferraro
 */
public interface CacheConfiguration {

	CacheProperties getCacheProperties();

	Supplier<Batch> getBatchFactory();
}
