/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.remote;

import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.batch.Batcher;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;

/**
 * @param <S> the container session facade type
 * @param <SC> the deployment context type
 * @param <AL> the container attribute listener type
 * @param <LC> the local context type
 * @author Paul Ferraro
 */
public interface HotRodSessionManagerFactoryConfiguration<S, SC, AL, LC> extends SessionManagerFactoryConfiguration<S, SC, AL, LC>, HotRodSessionFactoryConfiguration {

	@Override
	default Batcher<TransactionBatch> getBatcher() {
		return HotRodSessionFactoryConfiguration.super.getBatcher();
	}

	@Override
	default CacheProperties getCacheProperties() {
		return HotRodSessionFactoryConfiguration.super.getCacheProperties();
	}
}
