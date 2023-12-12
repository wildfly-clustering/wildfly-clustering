/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.remote;

import java.util.function.Consumer;

import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshalledValue;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.context.ContextStrategy;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionManagerConfiguration;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.cache.ContextualSessionManager;
import org.wildfly.clustering.session.cache.DelegatingSessionManagerConfiguration;
import org.wildfly.clustering.session.cache.SessionFactory;
import org.wildfly.clustering.session.cache.attributes.MarshalledValueMarshallerSessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;
import org.wildfly.clustering.session.cache.metadata.fine.SessionMetaDataEntry;
import org.wildfly.clustering.session.infinispan.remote.attributes.CoarseSessionAttributesFactory;
import org.wildfly.clustering.session.infinispan.remote.attributes.FineSessionAttributesFactory;
import org.wildfly.clustering.session.infinispan.remote.attributes.HotRodSessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.infinispan.remote.metadata.HotRodSessionMetaDataFactory;

/**
 * Factory for creating session managers.
 * @param <S> the HttpSession specification type
 * @param <SC> the ServletContext specification type
 * @param <AL> the HttpSessionAttributeListener specification type
 * @param <LC> the local context type
 * @author Paul Ferraro
 */
public class HotRodSessionManagerFactory<S, SC, AL, LC> implements SessionManagerFactory<SC, LC, TransactionBatch> {

	private final RemoteCacheConfiguration configuration;
	private final Registrar<Consumer<ImmutableSession>> expirationListenerRegistrar;
	private final SessionFactory<SC, SessionMetaDataEntry<LC>, ?, LC> factory;

	public HotRodSessionManagerFactory(HotRodSessionManagerFactoryConfiguration<S, SC, AL, LC> configuration) {
		this.configuration = configuration;
		SessionMetaDataFactory<SessionMetaDataEntry<LC>> metaDataFactory = new HotRodSessionMetaDataFactory<>(configuration);
		HotRodSessionFactory<SC, ?, LC> sessionFactory = new HotRodSessionFactory<>(configuration, metaDataFactory, this.createSessionAttributesFactory(configuration), configuration.getSessionContextFactory());
		this.factory = sessionFactory;
		this.expirationListenerRegistrar = sessionFactory;
	}

	@Override
	public SessionManager<LC, TransactionBatch> createSessionManager(SessionManagerConfiguration<SC, TransactionBatch> configuration) {
		Registrar<Consumer<ImmutableSession>> expirationListenerRegistrar = this.expirationListenerRegistrar;
		HotRodSessionManagerConfiguration<SC> config = new AbstractHotRodSessionManagerConfiguration<>(configuration, this.configuration) {
			@Override
			public Registrar<Consumer<ImmutableSession>> getExpirationListenerRegistrar() {
				return expirationListenerRegistrar;
			}
		};
		return new ContextualSessionManager<>(new HotRodSessionManager<>(config, this.factory), ContextStrategy.SHARED);
	}

	@Override
	public void close() {
		this.factory.close();
	}

	private SessionAttributesFactory<SC, ?> createSessionAttributesFactory(HotRodSessionManagerFactoryConfiguration<S, SC, AL, LC> configuration) {
		switch (configuration.getAttributePersistenceStrategy()) {
			case FINE: {
				return new FineSessionAttributesFactory<>(new HotRodMarshalledValueSessionAttributesFactoryConfiguration<>(configuration));
			}
			case COARSE: {
				return new CoarseSessionAttributesFactory<>(new HotRodMarshalledValueSessionAttributesFactoryConfiguration<>(configuration));
			}
			default: {
				// Impossible
				throw new IllegalStateException();
			}
		}
	}

	private abstract static class AbstractHotRodSessionManagerConfiguration<SC> extends DelegatingSessionManagerConfiguration<SC, TransactionBatch> implements HotRodSessionManagerConfiguration<SC> {
		private final RemoteCacheConfiguration configuration;

		AbstractHotRodSessionManagerConfiguration(SessionManagerConfiguration<SC, TransactionBatch> managerConfiguration, RemoteCacheConfiguration configuration) {
			super(managerConfiguration);
			this.configuration = configuration;
		}

		@Override
		public <CK, CV> RemoteCache<CK, CV> getCache() {
			return this.configuration.getCache();
		}
	}

	private static class HotRodMarshalledValueSessionAttributesFactoryConfiguration<S, SC, AL, V> extends MarshalledValueMarshallerSessionAttributesFactoryConfiguration<S, SC, AL, V> implements HotRodSessionAttributesFactoryConfiguration<S, SC, AL, V, MarshalledValue<V, ByteBufferMarshaller>> {
		private final RemoteCacheConfiguration configuration;

		<LC> HotRodMarshalledValueSessionAttributesFactoryConfiguration(HotRodSessionManagerFactoryConfiguration<S, SC, AL, LC> configuration) {
			super(configuration);
			this.configuration = configuration;
		}

		@Override
		public <CK, CV> RemoteCache<CK, CV> getCache() {
			return this.configuration.getCache();
		}
	}
}
