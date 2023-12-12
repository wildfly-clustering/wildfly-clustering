/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.remote;

import java.util.function.Consumer;

import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.batch.Batcher;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.SessionManagerConfiguration;

/**
 * Configuration for an {@link HotRodSessionManager}.
 * @param <C> the ServletContext specification type
 * @author Paul Ferraro
 */
public interface HotRodSessionManagerConfiguration<C> extends SessionManagerConfiguration<C, TransactionBatch>, RemoteCacheConfiguration {
	Registrar<Consumer<ImmutableSession>> getExpirationListenerRegistrar();

	@Override
	default CacheProperties getCacheProperties() {
		return RemoteCacheConfiguration.super.getCacheProperties();
	}

	@Override
	default Batcher<TransactionBatch> getBatcher() {
		return RemoteCacheConfiguration.super.getBatcher();
	}
}
