/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import java.util.concurrent.Executor;

import org.infinispan.commons.api.BasicCacheContainer;

/**
 * @author Paul Ferraro
 */
public interface BasicCacheContainerConfiguration {
	String getName();

	BasicCacheContainer getCacheContainer();

	Executor getExecutor();
}
