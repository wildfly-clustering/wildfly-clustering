/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.batch.Batcher;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.manager.IdentifierFactory;
import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionManagerConfiguration;

/**
 * Configuration for an {@link InfinispanSessionManager}.
 * @param <DC> the deployment context type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public interface InfinispanSessionManagerConfiguration<DC, SC> extends SessionManagerConfiguration<DC>, EmbeddedCacheConfiguration {

	Scheduler<String, ExpirationMetaData> getExpirationScheduler();
	Runnable getStartTask();
	Registrar<SessionManager<SC, TransactionBatch>> getRegistrar();

	@Override
	IdentifierFactory<String> getIdentifierFactory();

	@Override
	default Batcher<TransactionBatch> getBatcher() {
		return EmbeddedCacheConfiguration.super.getBatcher();
	}

	@Override
	default CacheProperties getCacheProperties() {
		return EmbeddedCacheConfiguration.super.getCacheProperties();
	}
}
