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
import java.util.function.Supplier;

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
 * @param <DC> the deployment context type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class HotRodSessionManagerFactory<DC, SC> implements SessionManagerFactory<DC, SC> {

	/**
	 * The configuration of this session manager factory.
	 * @param <S> the session specification type
	 * @param <DC> the deployment context type
	 * @param <SC> the session context type
	 * @param <L> the session event listener specification type
	 */
	public interface Configuration<S, DC, SC, L> {
		SessionManagerFactoryConfiguration<SC> getSessionManagerFactoryConfiguration();
		SessionSpecificationProvider<S, DC> getSessionSpecificationProvider();
		SessionEventListenerSpecificationProvider<S, L> getSessionEventListenerSpecificationProvider();
		RemoteCacheConfiguration getCacheConfiguration();
	}

	private final RemoteCacheConfiguration configuration;
	private final SessionFactory<DC, SessionMetaDataEntry<SC>, Object, SC> sessionFactory;
	private final Collection<Consumer<ImmutableSession>> expirationListeners = new CopyOnWriteArraySet<>();

	/**
	 * Creates a session manager factory.
	 * @param <S> the session specification type
	 * @param <L> the session event listener specification type
	 * @param configuration the configuration of this session manager factory
	 */
	public <S, L> HotRodSessionManagerFactory(Configuration<S, DC, SC, L> configuration) {
		this.configuration = configuration.getCacheConfiguration();
		SessionMetaDataFactory<SessionMetaDataEntry<SC>> metaDataFactory = new HotRodSessionMetaDataFactory<>(this.configuration);
		@SuppressWarnings("unchecked")
		SessionAttributesFactory<DC, Object> attributesFactory = (SessionAttributesFactory<DC, Object>) this.createSessionAttributesFactory(configuration);
		Consumer<ImmutableSession> listeners = org.wildfly.clustering.function.Consumer.acceptAll(this.expirationListeners);
		this.sessionFactory = new HotRodSessionFactory<>(new HotRodSessionFactory.Configuration<DC, Object, SC>() {
			@Override
			public SessionMetaDataFactory<SessionMetaDataEntry<SC>> getSessionMetaDataFactory() {
				return metaDataFactory;
			}

			@Override
			public SessionAttributesFactory<DC, Object> getSessionAttributesFactory() {
				return attributesFactory;
			}

			@Override
			public Supplier<SC> getSessionContextFactory() {
				return configuration.getSessionManagerFactoryConfiguration().getSessionContextFactory();
			}

			@Override
			public RemoteCacheConfiguration getCacheConfiguration() {
				return configuration.getCacheConfiguration();
			}

			@Override
			public Consumer<ImmutableSession> getSessionExpirationListener() {
				return listeners;
			}
		});
	}

	@Override
	public SessionManager<SC> createSessionManager(SessionManagerConfiguration<DC> configuration) {
		RemoteCacheConfiguration cacheConfiguration = this.configuration;
		SessionFactory<DC, SessionMetaDataEntry<SC>, Object, SC> sessionFactory = this.sessionFactory;
		IdentifierFactoryService<String> identifierFactory = new SimpleIdentifierFactoryService<>(configuration.getIdentifierFactory());
		Collection<Consumer<ImmutableSession>> expirationListeners = this.expirationListeners;
		Consumer<ImmutableSession> expirationListener = configuration.getExpirationListener();
		AtomicReference<SessionManager<SC>> reference = new AtomicReference<>();
		BiFunction<String, SC, Session<SC>> detachedSessionFactory = (id, context) -> new DetachedSession<>(reference::getPlain, id, context);
		SessionManager<SC> manager = new CachedSessionManager<>(new HotRodSessionManager<>(new HotRodSessionManager.Configuration<DC, SessionMetaDataEntry<SC>, Object, SC>() {
			@Override
			public IdentifierFactoryService<String> getIdentifierFactory() {
				return identifierFactory;
			}

			@Override
			public SessionFactory<DC, SessionMetaDataEntry<SC>, Object, SC> getSessionFactory() {
				return sessionFactory;
			}

			@Override
			public BiFunction<String, SC, Session<SC>> getDetachedSessionFactory() {
				return detachedSessionFactory;
			}

			@Override
			public DC getContext() {
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

	private <S, L> SessionAttributesFactory<DC, ?> createSessionAttributesFactory(Configuration<S, DC, SC, L> configuration) {
		switch (configuration.getSessionManagerFactoryConfiguration().getAttributePersistenceStrategy()) {
			case FINE -> {
				BiFunction<ImmutableSession, DC, SessionAttributeActivationNotifier> passivationNotifierFactory = (session, context) -> new ImmutableSessionAttributeActivationNotifier<>(configuration.getSessionSpecificationProvider(), configuration.getSessionEventListenerSpecificationProvider(), session, context);
				return new FineSessionAttributesFactory<>(new MarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration.getSessionManagerFactoryConfiguration()), passivationNotifierFactory, configuration.getCacheConfiguration());
			}
			case COARSE -> {
				BiFunction<ImmutableSession, DC, SessionActivationNotifier> passivationNotifierFactory = (session, context) -> new ImmutableSessionActivationNotifier<>(configuration.getSessionSpecificationProvider(), configuration.getSessionEventListenerSpecificationProvider(), session, context);
				return new CoarseSessionAttributesFactory<>(new MarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration.getSessionManagerFactoryConfiguration()), passivationNotifierFactory, configuration.getCacheConfiguration());
			}
			default -> throw new IllegalStateException();
		}
	}
}
