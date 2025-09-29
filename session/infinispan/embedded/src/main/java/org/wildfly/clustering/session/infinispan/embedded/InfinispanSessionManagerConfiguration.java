/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.manager.IdentifierFactoryService;
import org.wildfly.clustering.server.scheduler.Scheduler;

/**
 * Configuration for an {@link InfinispanSessionManager}.
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public interface InfinispanSessionManagerConfiguration<SC> extends EmbeddedCacheConfiguration {

	Scheduler<String, ExpirationMetaData> getExpirationScheduler();
	IdentifierFactoryService<String> getIdentifierFactory();
}
