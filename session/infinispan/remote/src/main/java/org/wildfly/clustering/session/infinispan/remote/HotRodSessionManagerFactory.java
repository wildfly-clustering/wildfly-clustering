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
 * @param <DC> the deployment specification type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class HotRodSessionManagerFactory<DC, SC> implements SessionManagerFactory<DC, SC, TransactionBatch>, HotRodSessionManagerConfiguration {

	private final RemoteCacheConfiguration configuration;
	private final Registrar<Consumer<ImmutableSession>> expirationListenerRegistrar;
	private final SessionFactory<DC, SessionMetaDataEntry<SC>, ?, SC> factory;

	public <S, L> HotRodSessionManagerFactory(SessionManagerFactoryConfiguration<S, DC, L, SC> configuration, HotRodSessionFactoryConfiguration sessionFactoryConfiguration) {
		this.configuration = sessionFactoryConfiguration;
		SessionMetaDataFactory<SessionMetaDataEntry<SC>> metaDataFactory = new HotRodSessionMetaDataFactory<>(sessionFactoryConfiguration);
		HotRodSessionFactory<DC, ?, SC> sessionFactory = new HotRodSessionFactory<>(sessionFactoryConfiguration, metaDataFactory, this.createSessionAttributesFactory(configuration, sessionFactoryConfiguration), configuration.getSessionContextFactory());
		this.factory = sessionFactory;
		this.expirationListenerRegistrar = sessionFactory;
	}

	@Override
	public SessionManager<SC, TransactionBatch> createSessionManager(SessionManagerConfiguration<DC> configuration) {
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

	private <S, L> SessionAttributesFactory<DC, ?> createSessionAttributesFactory(SessionManagerFactoryConfiguration<S, DC, L, SC> configuration, RemoteCacheConfiguration hotrod) {
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
