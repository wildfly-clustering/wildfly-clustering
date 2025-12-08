/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.remote;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.function.BiFunction;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.cache.CacheStrategy;
import org.wildfly.clustering.server.listener.ConsumerRegistry;
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
import org.wildfly.clustering.session.container.ContainerProvider;
import org.wildfly.clustering.session.infinispan.remote.attributes.CoarseSessionAttributesFactory;
import org.wildfly.clustering.session.infinispan.remote.attributes.FineSessionAttributesFactory;
import org.wildfly.clustering.session.infinispan.remote.metadata.HotRodSessionMetaDataFactory;

/**
 * Factory for creating session managers.
 * @param <CC> the container context type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class HotRodSessionManagerFactory<CC, SC> implements SessionManagerFactory<CC, SC> {

	private static final System.Logger LOGGER = System.getLogger(HotRodSessionManagerFactory.class.getName());

	/**
	 * The configuration of this session manager factory.
	 * @param <SC> the session context type
	 */
	public interface Configuration<SC> {
		/**
		 * Returns the configuration of the session manager factory.
		 * @return the configuration of the session manager factory.
		 */
		SessionManagerFactoryConfiguration<SC> getSessionManagerFactoryConfiguration();

		/**
		 * Returns the configuration for the associated cache.
		 * @return the configuration for the associated cache.
		 */
		RemoteCacheConfiguration getCacheConfiguration();
	}

	private final RemoteCacheConfiguration configuration;
	private final Function<SessionManagerConfiguration<CC>, Registrar<SessionManager<SC>>> managerRegistrarFactory;
	private final SessionFactory<CC, SessionMetaDataEntry<SC>, Object, SC> sessionFactory;
	private final Function<CC, String> contextIdentifier;
	private final Map<String, SessionManager<SC>> managers = new ConcurrentHashMap<>();

	/**
	 * Creates a session manager factory.
	 * @param <S> the session specification type
	 * @param <L> the session event listener specification type
	 * @param configuration the configuration of this session manager factory
	 */
	public <S, L> HotRodSessionManagerFactory(Configuration<SC> configuration) {
		ContainerProvider<CC, S, L, SC> provider = ServiceLoader.load(ContainerProvider.class, ContainerProvider.class.getClassLoader()).findFirst().orElseThrow();
		LOGGER.log(System.Logger.Level.DEBUG, "{0} configured for {1} container", this.getClass().getSimpleName(), provider);
		this.contextIdentifier = provider::getId;
		this.configuration = configuration.getCacheConfiguration();
		SessionMetaDataFactory<SessionMetaDataEntry<SC>> metaDataFactory = new HotRodSessionMetaDataFactory<>(this.configuration);
		@SuppressWarnings("unchecked")
		SessionAttributesFactory<CC, Object> attributesFactory = (SessionAttributesFactory<CC, Object>) this.createSessionAttributesFactory(configuration, provider);
		ConsumerRegistry<ImmutableSession> expirationListenerRegistry = ConsumerRegistry.newInstance();
		this.sessionFactory = new HotRodSessionFactory<>(new HotRodSessionFactory.Configuration<CC, Object, SC>() {
			@Override
			public SessionMetaDataFactory<SessionMetaDataEntry<SC>> getSessionMetaDataFactory() {
				return metaDataFactory;
			}

			@Override
			public SessionAttributesFactory<CC, Object> getSessionAttributesFactory() {
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
			public java.util.function.Consumer<ImmutableSession> getSessionExpirationListener() {
				return expirationListenerRegistry;
			}
		});
		this.managerRegistrarFactory = new Function<>() {
			@Override
			public Registrar<SessionManager<SC>> apply(SessionManagerConfiguration<CC> managerConfiguration) {
				return new Registrar<>() {
					@Override
					public Registration register(SessionManager<SC> manager) {
						CC context = managerConfiguration.getContext();
						String contextId = provider.getId(context);
						HotRodSessionManagerFactory.this.managers.put(contextId, manager);
						Registration managerRegistration = () -> HotRodSessionManagerFactory.this.managers.remove(contextId);
						Registration expirationRegistration = expirationListenerRegistry.register(managerConfiguration.getExpirationListener());
						return Registration.composite(List.of(expirationRegistration, managerRegistration));
					}
				};
			}
		};
	}

	SessionManager<SC> findSessionManager(CC context) {
		return this.managers.get(this.contextIdentifier.apply(context));
	}

	@Override
	public SessionManager<SC> createSessionManager(SessionManagerConfiguration<CC> configuration) {
		RemoteCacheConfiguration cacheConfiguration = this.configuration;
		SessionFactory<CC, SessionMetaDataEntry<SC>, Object, SC> sessionFactory = this.sessionFactory;
		IdentifierFactoryService<String> identifierFactory = new SimpleIdentifierFactoryService<>(configuration.getIdentifierFactory());
		BiFunction<String, SC, Session<SC>> detachedSessionFactory = (id, context) -> Optional.ofNullable(this.findSessionManager(configuration.getContext())).map(manager -> new DetachedSession<>(manager, id, context)).orElse(null);
		Registrar<SessionManager<SC>> registrar = this.managerRegistrarFactory.apply(configuration);
		SessionManager<SC> manager = new CachedSessionManager<>(new HotRodSessionManager<>(new HotRodSessionManager.Configuration<CC, SessionMetaDataEntry<SC>, Object, SC>() {
			@Override
			public IdentifierFactoryService<String> getIdentifierFactory() {
				return identifierFactory;
			}

			@Override
			public SessionFactory<CC, SessionMetaDataEntry<SC>, Object, SC> getSessionFactory() {
				return sessionFactory;
			}

			@Override
			public BiFunction<String, SC, Session<SC>> getDetachedSessionFactory() {
				return detachedSessionFactory;
			}

			@Override
			public CC getContext() {
				return configuration.getContext();
			}

			@Override
			public java.util.function.Consumer<ImmutableSession> getExpirationListener() {
				return configuration.getExpirationListener();
			}

			@Override
			public Optional<Duration> getMaxIdle() {
				return configuration.getMaxIdle();
			}

			@Override
			public RemoteCacheConfiguration getCacheConfiguration() {
				return cacheConfiguration;
			}
		}), CacheStrategy.CONCURRENT) {
			private final AtomicReference<Registration> registration = new AtomicReference<>();

			@Override
			public void start() {
				this.registration.set(registrar.register(this));
				super.start();
			}

			@Override
			public void stop() {
				try {
					super.stop();
				} finally {
					Consumer.close().accept(this.registration.getAndSet(null));
				}
			}
		};
		return manager;
	}

	@Override
	public void close() {
		this.sessionFactory.close();
	}

	private <S, L> SessionAttributesFactory<CC, ?> createSessionAttributesFactory(Configuration<SC> configuration, ContainerProvider<CC, S, L, SC> provider) {
		switch (configuration.getSessionManagerFactoryConfiguration().getAttributePersistenceStrategy()) {
			case FINE -> {
				BiFunction<ImmutableSession, CC, SessionAttributeActivationNotifier> passivationNotifierFactory = (session, context) -> Optional.ofNullable(this.findSessionManager(context)).map(manager -> new ImmutableSessionAttributeActivationNotifier<>(provider, provider.getDetachableSession(manager, session, context))).orElse(null);
				return new FineSessionAttributesFactory<>(new MarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration.getSessionManagerFactoryConfiguration()), passivationNotifierFactory, configuration.getCacheConfiguration());
			}
			case COARSE -> {
				BiFunction<ImmutableSession, CC, SessionActivationNotifier> passivationNotifierFactory = (session, context) -> Optional.ofNullable(this.findSessionManager(context)).map(manager -> new ImmutableSessionActivationNotifier<>(provider, provider.getDetachableSession(manager, session, context), session.getAttributes().values())).orElse(null);
				return new CoarseSessionAttributesFactory<>(new MarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration.getSessionManagerFactoryConfiguration()), passivationNotifierFactory, configuration.getCacheConfiguration());
			}
			default -> throw new IllegalStateException();
		}
	}
}
