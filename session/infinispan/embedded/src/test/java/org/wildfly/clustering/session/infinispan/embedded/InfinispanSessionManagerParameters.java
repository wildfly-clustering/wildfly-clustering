/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.time.Duration;

import org.infinispan.configuration.cache.CacheType;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.transaction.TransactionMode;
import org.wildfly.clustering.function.Runner;
import org.wildfly.clustering.session.cache.SessionManagerParameters;

/**
 * @author Paul Ferraro
 */
public interface InfinispanSessionManagerParameters extends SessionManagerParameters {
	CacheType getCacheType();
	TransactionMode getTransactionMode();

	@Override
	default String getDeploymentName() {
		return String.format("%s-%s-%s-%s.war", this.getSessionAttributeMarshaller(), this.getSessionAttributePersistenceStrategy(), this.getCacheType(), this.getTransactionMode());
	}

	@Override
	default Duration getFailoverGracePeriod() {
		return !this.getTransactionMode().isTransactional() ? Duration.ofSeconds(1) : SessionManagerParameters.super.getFailoverGracePeriod();
	}

	default Runnable persistence(@SuppressWarnings("unused") GlobalConfiguration global, @SuppressWarnings("unused") PersistenceConfigurationBuilder builder) {
		return Runner.of();
	}
}
