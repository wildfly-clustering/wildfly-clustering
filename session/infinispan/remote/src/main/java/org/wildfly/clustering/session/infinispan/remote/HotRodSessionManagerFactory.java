/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.remote;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.server.cache.CacheStrategy;
import org.wildfly.clustering.server.local.manager.SimpleIdentifierFactoryService;
import org.wildfly.clustering.server.manager.IdentifierFactoryService;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionManagerConfiguration;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;
import org.wildfly.clustering.session.cache.CachedSessionManager;
import org.wildfly.clustering.session.cache.DetachedSession;
import org.wildfly.clustering.session.cache.SessionFactory;
import org.wildfly.clustering.session.cache.attributes.MarshalledValueMarshallerSessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.attributes.coarse.ImmutableSessionActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.coarse.SessionActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.fine.ImmutableSessionAttributeActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;
import org.wildfly.clustering.session.cache.metadata.fine.SessionMetaDataEntry;
import org.wildfly.clustering.session.infinispan.remote.attributes.CoarseSessionAttributesFactory;
import org.wildfly.clustering.session.infinispan.remote.attributes.FineSessionAttributesFactory;
import org.wildfly.clustering.session.infinispan.remote.metadata.HotRodSessionMetaDataFactory;
import org.wildfly.clustering.session.spec.SessionEventListenerSpecificationProvider;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;

/**
 * Factory for creating session managers.
 * @param <C> the session manager context type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class HotRodSessionManagerFactory<C, SC> implements SessionManagerFactory<C, SC> {

	private final RemoteCacheConfiguration configuration;
	private final SessionFactory<C, SessionMetaDataEntry<SC>, Object, SC> sessionFactory;
	private final Collection<Consumer<ImmutableSession>> expirationListeners = new CopyOnWriteArraySet<>();

	public <S, L> HotRodSessionManagerFactory(SessionManagerFactoryConfiguration<SC> configuration, SessionSpecificationProvider<S, C> sessionProvider, SessionEventListenerSpecificationProvider<S, L> listenerProvider, RemoteCacheConfiguration sessionFactoryConfiguration) {
		this.configuration = sessionFactoryConfiguration;
		SessionMetaDataFactory<SessionMetaDataEntry<SC>> metaDataFactory = new HotRodSessionMetaDataFactory<>(sessionFactoryConfiguration);
		@SuppressWarnings("unchecked")
		SessionAttributesFactory<C, Object> attributesFactory = (SessionAttributesFactory<C, Object>) this.createSessionAttributesFactory(configuration, sessionProvider, listenerProvider, sessionFactoryConfiguration);
		this.sessionFactory = new HotRodSessionFactory<>(sessionFactoryConfiguration, metaDataFactory, attributesFactory, configuration.getSessionContextFactory(), org.wildfly.clustering.function.Consumer.acceptAll(this.expirationListeners));
	}

	@Override
	public SessionManager<SC> createSessionManager(SessionManagerConfiguration<C> configuration) {
		RemoteCacheConfiguration cacheConfiguration = this.configuration;
		SessionFactory<C, SessionMetaDataEntry<SC>, Object, SC> sessionFactory = this.sessionFactory;
		IdentifierFactoryService<String> identifierFactory = new SimpleIdentifierFactoryService<>(configuration.getIdentifierFactory());
		Collection<Consumer<ImmutableSession>> expirationListeners = this.expirationListeners;
		Consumer<ImmutableSession> expirationListener = configuration.getExpirationListener();
		AtomicReference<SessionManager<SC>> reference = new AtomicReference<>();
		BiFunction<String, SC, Session<SC>> detachedSessionFactory = (id, context) -> new DetachedSession<>(reference::getPlain, id, context);
		SessionManager<SC> manager = new CachedSessionManager<>(new HotRodSessionManager<>(new HotRodSessionManager.Configuration<C, SessionMetaDataEntry<SC>, Object, SC>() {
			@Override
			public IdentifierFactoryService<String> getIdentifierFactory() {
				return identifierFactory;
			}

			@Override
			public SessionFactory<C, SessionMetaDataEntry<SC>, Object, SC> getSessionFactory() {
				return sessionFactory;
			}

			@Override
			public BiFunction<String, SC, Session<SC>> getDetachedSessionFactory() {
				return detachedSessionFactory;
			}

			@Override
			public C getContext() {
				return configuration.getContext();
			}

			@Override
			public Consumer<ImmutableSession> getExpirationListener() {
				return configuration.getExpirationListener();
			}

			@Override
			public Duration getTimeout() {
				return configuration.getTimeout();
			}

			@Override
			public RemoteCacheConfiguration getCacheConfiguration() {
				return cacheConfiguration;
			}
		}), CacheStrategy.CONCURRENT) {
			@Override
			public void start() {
				expirationListeners.add(expirationListener);
				super.start();
			}

			@Override
			public void stop() {
				try {
					super.stop();
				} finally {
					expirationListeners.remove(expirationListener);
				}
			}
		};
		reference.setPlain(manager);
		return manager;
	}

	@Override
	public void close() {
		this.sessionFactory.close();
	}

	private <S, L> SessionAttributesFactory<C, ?> createSessionAttributesFactory(SessionManagerFactoryConfiguration<SC> configuration, SessionSpecificationProvider<S, C> sessionProvider, SessionEventListenerSpecificationProvider<S, L> listenerProvider, RemoteCacheConfiguration hotrod) {
		switch (configuration.getAttributePersistenceStrategy()) {
			case FINE -> {
				BiFunction<ImmutableSession, C, SessionAttributeActivationNotifier> passivationNotifierFactory = (session, context) -> new ImmutableSessionAttributeActivationNotifier<>(sessionProvider, listenerProvider, session, context);
				return new FineSessionAttributesFactory<>(new MarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration), passivationNotifierFactory, hotrod);
			}
			case COARSE -> {
				BiFunction<ImmutableSession, C, SessionActivationNotifier> passivationNotifierFactory = (session, context) -> new ImmutableSessionActivationNotifier<>(sessionProvider, listenerProvider, session, context);
				return new CoarseSessionAttributesFactory<>(new MarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration), passivationNotifierFactory, hotrod);
			}
			default -> throw new IllegalStateException();
		}
	}
}
