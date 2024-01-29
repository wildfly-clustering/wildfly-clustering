/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.batch.Batcher;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.group.GroupMember;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;

/**
 * @param <S> the HttpSession specification type
 * @param <DC> the ServletContext specification type
 * @param <AL> the HttpSessionAttributeListener specification type
 * @param <SC> the local context type
 * @param <GM> the group member type
 * @author Paul Ferraro
 */
public interface InfinispanSessionManagerFactoryConfiguration<S, DC, AL, SC, GM extends GroupMember<Address>> extends SessionManagerFactoryConfiguration<S, DC, AL, SC, TransactionBatch>, EmbeddedCacheConfiguration {

	GroupCommandDispatcherFactory<Address, GM> getCommandDispatcherFactory();

	@Override
	default CacheProperties getCacheProperties() {
		return EmbeddedCacheConfiguration.super.getCacheProperties();
	}

	@Override
	default Batcher<TransactionBatch> getBatcher() {
		return EmbeddedCacheConfiguration.super.getBatcher();
	}
}
