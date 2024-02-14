/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.remote;

import java.util.function.Consumer;

import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.context.ContextStrategy;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionManagerConfiguration;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;
import org.wildfly.clustering.session.cache.ContextualSessionManager;
import org.wildfly.clustering.session.cache.SessionFactory;
import org.wildfly.clustering.session.cache.attributes.MarshalledValueMarshallerSessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;
import org.wildfly.clustering.session.cache.metadata.fine.SessionMetaDataEntry;
import org.wildfly.clustering.session.infinispan.remote.attributes.CoarseSessionAttributesFactory;
import org.wildfly.clustering.session.infinispan.remote.attributes.FineSessionAttributesFactory;
import org.wildfly.clustering.session.infinispan.remote.metadata.HotRodSessionMetaDataFactory;

/**
 * Factory for creating session managers.
 * @param <S> the HttpSession specification type
 * @param <SC> the ServletContext specification type
 * @param <AL> the HttpSessionAttributeListener specification type
 * @param <LC> the local context type
 * @author Paul Ferraro
 */
public class HotRodSessionManagerFactory<S, SC, AL, LC> implements SessionManagerFactory<SC, LC, TransactionBatch>, HotRodSessionManagerConfiguration {

	private final RemoteCacheConfiguration configuration;
	private final Registrar<Consumer<ImmutableSession>> expirationListenerRegistrar;
	private final SessionFactory<SC, SessionMetaDataEntry<LC>, ?, LC> factory;

	public HotRodSessionManagerFactory(SessionManagerFactoryConfiguration<S, SC, AL, LC> configuration, HotRodSessionFactoryConfiguration sessionFactoryConfiguration) {
		this.configuration = sessionFactoryConfiguration;
		SessionMetaDataFactory<SessionMetaDataEntry<LC>> metaDataFactory = new HotRodSessionMetaDataFactory<>(sessionFactoryConfiguration);
		HotRodSessionFactory<SC, ?, LC> sessionFactory = new HotRodSessionFactory<>(sessionFactoryConfiguration, metaDataFactory, this.createSessionAttributesFactory(configuration, sessionFactoryConfiguration), configuration.getSessionContextFactory());
		this.factory = sessionFactory;
		this.expirationListenerRegistrar = sessionFactory;
	}

	@Override
	public SessionManager<LC, TransactionBatch> createSessionManager(SessionManagerConfiguration<SC> configuration) {
		return new ContextualSessionManager<>(new HotRodSessionManager<>(configuration, this.factory, this), ContextStrategy.SHARED);
	}

	@Override
	public void close() {
		this.factory.close();
	}

	@Override
	public <CK, CV> RemoteCache<CK, CV> getCache() {
		return this.configuration.getCache();
	}

	@Override
	public Registrar<Consumer<ImmutableSession>> getExpirationListenerRegistrar() {
		return this.expirationListenerRegistrar;
	}

	private SessionAttributesFactory<SC, ?> createSessionAttributesFactory(SessionManagerFactoryConfiguration<S, SC, AL, LC> configuration, RemoteCacheConfiguration hotrod) {
		switch (configuration.getAttributePersistenceStrategy()) {
			case FINE: {
				return new FineSessionAttributesFactory<>(new MarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration), hotrod);
			}
			case COARSE: {
				return new CoarseSessionAttributesFactory<>(new MarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration), hotrod);
			}
			default: {
				// Impossible
				throw new IllegalStateException();
			}
		}
	}
}
