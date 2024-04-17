/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded.user;

import java.util.Map;

import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.marshalling.ByteBufferMarshalledValueFactory;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshalledValue;
import org.wildfly.clustering.marshalling.MarshalledValueMarshaller;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.infinispan.manager.AffinityIdentifierFactory;
import org.wildfly.clustering.server.manager.IdentifierFactory;
import org.wildfly.clustering.session.cache.user.CompositeUserFactory;
import org.wildfly.clustering.session.cache.user.DefaultUserManager;
import org.wildfly.clustering.session.cache.user.UserContext;
import org.wildfly.clustering.session.cache.user.UserContextFactory;
import org.wildfly.clustering.session.cache.user.UserFactory;
import org.wildfly.clustering.session.cache.user.UserSessionsFactory;
import org.wildfly.clustering.session.user.UserManager;
import org.wildfly.clustering.session.user.UserManagerConfiguration;
import org.wildfly.clustering.session.user.UserManagerFactory;

/**
 * Embedded Infinispan cache-based user manager factory.
 * @param <C> the persistent context type
 * @param <D> the deployment type
 * @param <S> the session type
 * @author Paul Ferraro
 */
public class InfinispanUserManagerFactory<C, D, S> implements UserManagerFactory<C, D, S, TransactionBatch> {

	private final EmbeddedCacheConfiguration configuration;

	public InfinispanUserManagerFactory(EmbeddedCacheConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public <T> UserManager<C, T, D, S, TransactionBatch> createUserManager(UserManagerConfiguration<T> configuration) {
		Marshaller<C, MarshalledValue<C, ByteBufferMarshaller>> marshaller = new MarshalledValueMarshaller<>(new ByteBufferMarshalledValueFactory(configuration.getMarshaller()));
		UserContextFactory<UserContext<MarshalledValue<C, ByteBufferMarshaller>, T>, C, T> contextFactory = new InfinispanUserContextFactory<>(this.configuration, marshaller, configuration.getTransientContextFactory());
		UserSessionsFactory<Map<D, S>, D, S> sessionsFactory = new InfinispanUserSessionsFactory<>(this.configuration);
		UserFactory<UserContext<MarshalledValue<C, ByteBufferMarshaller>, T>, C, T, Map<D, S>, D, S> factory = new CompositeUserFactory<>(contextFactory, sessionsFactory);
		IdentifierFactory<String> identifierFactory = new AffinityIdentifierFactory<>(configuration.getIdentifierFactory(), this.configuration.getCache());
		return new DefaultUserManager<>(factory, identifierFactory, this.configuration.getBatcher());
	}
}
