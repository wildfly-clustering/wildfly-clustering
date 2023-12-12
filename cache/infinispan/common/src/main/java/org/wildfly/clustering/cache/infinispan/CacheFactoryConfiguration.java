/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

/**
 * @author Paul Ferraro
 */
public interface CacheFactoryConfiguration {

	String getContainerName();

	String getConfigurationName();
}
