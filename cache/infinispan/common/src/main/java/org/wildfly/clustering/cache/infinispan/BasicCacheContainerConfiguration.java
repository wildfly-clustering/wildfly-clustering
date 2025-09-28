/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import java.util.concurrent.Executor;

import org.infinispan.commons.api.BasicCacheContainer;

/**
 * Infinispan cache configuration specialization for a {@link BasicCacheContainer}.
 * @author Paul Ferraro
 */
public interface BasicCacheContainerConfiguration {
	/**
	 * Returns the name of the associated cache container.
	 * @return the name of the associated cache container.
	 */
	String getName();

	/**
	 * Returns the associated cache container.
	 * @return the associated cache container.
	 */
	BasicCacheContainer getCacheContainer();

	/**
	 * Returns the executor associated with this cache container.
	 * @return the executor associated with this cache container.
	 */
	Executor getExecutor();
}
