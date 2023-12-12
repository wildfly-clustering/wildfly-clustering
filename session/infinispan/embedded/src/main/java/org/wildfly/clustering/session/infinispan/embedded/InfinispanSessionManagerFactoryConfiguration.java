/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.batch.Batcher;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.server.infinispan.dispatcher.CacheContainerCommandDispatcherFactory;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;

/**
 * @param <S> the HttpSession specification type
 * @param <DC> the ServletContext specification type
 * @param <AL> the HttpSessionAttributeListener specification type
 * @param <SC> the local context type
 * @author Paul Ferraro
 */
public interface InfinispanSessionManagerFactoryConfiguration<S, DC, AL, SC> extends SessionManagerFactoryConfiguration<S, DC, AL, SC, TransactionBatch>, EmbeddedCacheConfiguration {

	CacheContainerCommandDispatcherFactory getCommandDispatcherFactory();

	@Override
	default CacheProperties getCacheProperties() {
		return EmbeddedCacheConfiguration.super.getCacheProperties();
	}

	@Override
	default Batcher<TransactionBatch> getBatcher() {
		return EmbeddedCacheConfiguration.super.getBatcher();
	}
}
