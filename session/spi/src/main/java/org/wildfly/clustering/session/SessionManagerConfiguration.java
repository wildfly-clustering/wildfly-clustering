/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import org.wildfly.clustering.cache.CacheConfiguration;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.server.expiration.ExpirationConfiguration;
import org.wildfly.clustering.server.manager.ManagerConfiguration;

/**
 * Encapsulates the configuration of a session manager.
 * @author Paul Ferraro
 * @param <C> the session manager context type
 */
public interface SessionManagerConfiguration<C, B extends Batch> extends ManagerConfiguration<String, B>, ExpirationConfiguration<ImmutableSession>, CacheConfiguration<B> {
	/**
	 * Returns the container-specific context of this session manager.
	 * @return a container-specific session manager context
	 */
	C getContext();
}
