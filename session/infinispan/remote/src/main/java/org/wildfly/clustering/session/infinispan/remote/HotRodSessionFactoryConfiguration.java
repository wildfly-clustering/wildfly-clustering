/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;

/**
 * Encapsulates the configuration of a {@link HotRodSessionFactory}.
 * @author Paul Ferraro
 */
public interface HotRodSessionFactoryConfiguration extends RemoteCacheConfiguration {
	/**
	 * Returns the size of the thread pool used for processing expiration events from the remote Infinispan cluster.
	 * @return
	 */
	int getExpirationThreadPoolSize();
}
