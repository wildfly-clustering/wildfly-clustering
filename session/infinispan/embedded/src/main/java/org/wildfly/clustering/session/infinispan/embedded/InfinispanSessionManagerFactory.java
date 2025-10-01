/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import io.github.resilience4j.retry.RetryConfig;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.CacheStreamFilter;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistration;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.cache.CacheStrategy;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.dispatcher.CacheContainerCommandDispatcherFactory;
import org.wildfly.clustering.server.infinispan.expiration.ScheduleWithExpirationMetaDataCommand;
import org.wildfly.clustering.server.infinispan.manager.AffinityIdentifierFactoryService;
import org.wildfly.clustering.server.infinispan.scheduler.CacheEntriesTask;
import org.wildfly.clustering.server.infinispan.scheduler.CacheEntryScheduler;
import org.wildfly.clustering.server.infinispan.scheduler.CacheKeysTask;
import org.wildfly.clustering.server.infinispan.scheduler.PrimaryOwnerScheduler;
import org.wildfly.clustering.server.infinispan.scheduler.PrimaryOwnerSchedulerConfiguration;
import org.wildfly.clustering.server.infinispan.scheduler.ScheduleCommand;
import org.wildfly.clustering.server.infinispan.scheduler.ScheduleWithTransientMetaDataCommand;
import org.wildfly.clustering.server.infinispan.scheduler.Scheduler;
import org.wildfly.clustering.server.infinispan.scheduler.SchedulerTopologyChangeListener;
import org.wildfly.clustering.server.manager.IdentifierFactoryService;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionManagerConfiguration;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;
import org.wildfly.clustering.session.cache.CachedSessionManager;
import org.wildfly.clustering.session.cache.CompositeSessionFactory;
import org.wildfly.clustering.session.cache.DetachedSession;
import org.wildfly.clustering.session.cache.SessionFactory;
import org.wildfly.clustering.session.cache.attributes.IdentityMarshallerSessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.MarshalledValueMarshallerSessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.attributes.coarse.ImmutableSessionActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.coarse.SessionActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.fine.ImmutableSessionAttributeActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;
import org.wildfly.clustering.session.cache.metadata.coarse.ContextualSessionMetaDataEntry;
import org.wildfly.clustering.session.infinispan.embedded.attributes.CoarseSessionAttributesFactory;
import org.wildfly.clustering.session.infinispan.embedded.attributes.FineSessionAttributesFactory;
import org.wildfly.clustering.session.infinispan.embedded.metadata.InfinispanSessionMetaDataFactory;
import org.wildfly.clustering.session.infinispan.embedded.metadata.SessionMetaDataKey;
import org.wildfly.clustering.session.spec.SessionEventListenerSpecificationProvider;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;

/**
 * Factory for creating session managers.
 * @param <C> the session manager context type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class InfinispanSessionManagerFactory<C, SC> implements SessionManagerFactory<C, SC> {
	private final Scheduler<String, ExpirationMetaData> scheduler;
	private final SessionFactory<C, ContextualSessionMetaDataEntry<SC>, Object, SC> factory;
	private final Consumer<CacheStreamFilter<Map.Entry<SessionMetaDataKey, ContextualSessionMetaDataEntry<SC>>>> scheduleTask;
	private final ListenerRegistration schedulerListenerRegistration;
	private final EmbeddedCacheConfiguration configuration;
	private final Function<SessionManagerConfiguration<C>, Registrar<SessionManager<SC>>> managerRegistrarFactory;

	/**
	 * Creates a session manager factory.
	 * @param <S> the session manager context type
	 * @param <L> the specification type for a session passivation listener
	 * @param configuration the configuration of this factory
	 * @param sessionProvider the session specification provider
	 * @param listenerProvider a specification provider for session listeners
	 * @param infinispan the configuration of the associated cache
	 */
	public <S, L> InfinispanSessionManagerFactory(SessionManagerFactoryConfiguration<SC> configuration, SessionSpecificationProvider<S, C> sessionProvider, SessionEventListenerSpecificationProvider<S, L> listenerProvider, InfinispanSessionManagerFactoryConfiguration infinispan) {
		this.configuration = infinispan;
		SessionAttributeActivationNotifierFactory<S, C, L, SC> notifierFactory = new SessionAttributeActivationNotifierFactory<>(sessionProvider, listenerProvider);
		CacheProperties properties = infinispan.getCacheProperties();
		SessionMetaDataFactory<ContextualSessionMetaDataEntry<SC>> metaDataFactory = new InfinispanSessionMetaDataFactory<>(infinispan);
		@SuppressWarnings("unchecked")
		SessionAttributesFactory<C, Object> attributesFactory = (SessionAttributesFactory<C, Object>) this.createSessionAttributesFactory(configuration, sessionProvider, listenerProvider, notifierFactory, infinispan);
		this.factory = (SessionFactory<C, ContextualSessionMetaDataEntry<SC>, Object, SC>) new CompositeSessionFactory<>(metaDataFactory, attributesFactory, properties, configuration.getSessionContextFactory());
		ExpiredSessionRemover<C, ?, ?, SC> remover = new ExpiredSessionRemover<>(this.factory);
		this.managerRegistrarFactory = new Function<>() {
			@Override
			public Registrar<SessionManager<SC>> apply(SessionManagerConfiguration<C> managerConfiguration) {
				return new Registrar<>() {
					@Override
					public Registration register(SessionManager<SC> manager) {
						Registration contextRegistration = notifierFactory.register(Map.entry(managerConfiguration.getContext(), manager));
						Registration expirationRegistration = remover.register(managerConfiguration.getExpirationListener());
						return () -> {
							expirationRegistration.close();
							contextRegistration.close();
						};
					}
				};
			}
		};
		Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<SC>> cache = infinispan.getCache();
		CacheEntryScheduler<String, SessionMetaDataKey, ContextualSessionMetaDataEntry<SC>, ExpirationMetaData> localScheduler = new SessionExpirationScheduler<>(cache.getName(), infinispan.getBatchFactory(), this.factory.getMetaDataFactory(), remover, Duration.ofMillis(cache.getCacheConfiguration().transaction().cacheStopTimeout()));
		CacheContainerCommandDispatcherFactory dispatcherFactory = infinispan.getCommandDispatcherFactory();
		CacheContainerGroup group = dispatcherFactory.getGroup();
		this.scheduler = group.isSingleton() ? localScheduler : new PrimaryOwnerScheduler<>(new PrimaryOwnerSchedulerConfiguration<String, ExpirationMetaData>() {
			@Override
			public String getName() {
				return cache.getName();
			}

			@Override
			public CacheContainerCommandDispatcherFactory getCommandDispatcherFactory() {
				return dispatcherFactory;
			}

			@Override
			public Scheduler<String, ExpirationMetaData> getScheduler() {
				return localScheduler;
			}

			@Override
			public Cache<? extends Key<String>, ?> getCache() {
				return cache;
			}

			@Override
			public BiFunction<String, ExpirationMetaData, ScheduleCommand<String, ExpirationMetaData>> getScheduleCommandFactory() {
				return properties.isTransactional() ? ScheduleWithExpirationMetaDataCommand::new : ScheduleWithTransientMetaDataCommand::new;
			}

			@Override
			public RetryConfig getRetryConfig() {
				return infinispan.getRetryConfig();
			}
		});

		this.scheduleTask = CacheEntriesTask.schedule(cache, SessionCacheEntryFilter.META_DATA.cast(), localScheduler);
		Consumer<CacheStreamFilter<SessionMetaDataKey>> cancelTask = CacheKeysTask.cancel(cache, SessionCacheKeyFilter.META_DATA, localScheduler);
		this.schedulerListenerRegistration = new SchedulerTopologyChangeListener<>(cache, this.scheduleTask, cancelTask).register();
	}

	@Override
	public SessionManager<SC> createSessionManager(SessionManagerConfiguration<C> configuration) {
		EmbeddedCacheConfiguration cacheConfiguration = this.configuration;
		SessionFactory<C, ContextualSessionMetaDataEntry<SC>, Object, SC> sessionFactory = this.factory;
		IdentifierFactoryService<String> identifierFactory = new AffinityIdentifierFactoryService<>(configuration.getIdentifierFactory(), cacheConfiguration.getCache());
		Registrar<SessionManager<SC>> registrar = this.managerRegistrarFactory.apply(configuration);
		Scheduler<String, ExpirationMetaData> scheduler = this.scheduler;
		Runnable startTask = () -> this.scheduleTask.accept(CacheStreamFilter.local(cacheConfiguration.getCache()));
		AtomicReference<SessionManager<SC>> reference = new AtomicReference<>();
		BiFunction<String, SC, Session<SC>> detachedSessionFactory = (id, context) -> new DetachedSession<>(reference::getPlain, id, context);
		SessionManager<SC> manager = new CachedSessionManager<>(new InfinispanSessionManager<>(new InfinispanSessionManager.Configuration<C, ContextualSessionMetaDataEntry<SC>, Object, SC>() {
			@Override
			public IdentifierFactoryService<String> getIdentifierFactory() {
				return identifierFactory;
			}

			@Override
			public SessionFactory<C, ContextualSessionMetaDataEntry<SC>, Object, SC> getSessionFactory() {
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
			public EmbeddedCacheConfiguration getCacheConfiguration() {
				return cacheConfiguration;
			}

			@Override
			public Scheduler<String, ExpirationMetaData> getExpirationScheduler() {
				return scheduler;
			}
		}), CacheStrategy.CONCURRENT) {
			private final AtomicReference<Registration> registration = new AtomicReference<>();

			@Override
			public void start() {
				this.registration.set(registrar.register(this));
				super.start();
				startTask.run();
			}

			@Override
			public void stop() {
				try (Registration registration = this.registration.getAndSet(null)) {
					super.stop();
				}
			}
		};
		reference.setPlain(manager);
		return manager;
	}

	private <S, L> SessionAttributesFactory<C, ?> createSessionAttributesFactory(SessionManagerFactoryConfiguration<SC> configuration, SessionSpecificationProvider<S, C> sessionProvider, SessionEventListenerSpecificationProvider<S, L> listenerProvider, Function<String, SessionAttributeActivationNotifier> detachedPassivationNotifierFactory, EmbeddedCacheConfiguration infinispan) {
		boolean marshalling = infinispan.getCacheProperties().isMarshalling();
		switch (configuration.getAttributePersistenceStrategy()) {
			case FINE -> {
				BiFunction<ImmutableSession, C, SessionAttributeActivationNotifier> passivationNotifierFactory = (session, context) -> new ImmutableSessionAttributeActivationNotifier<>(sessionProvider, listenerProvider, session, context);
				return marshalling ? new FineSessionAttributesFactory<>(new MarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration), passivationNotifierFactory, detachedPassivationNotifierFactory, infinispan) : new FineSessionAttributesFactory<>(new IdentityMarshallerSessionAttributesFactoryConfiguration<>(configuration), passivationNotifierFactory, detachedPassivationNotifierFactory, infinispan);
			}
			case COARSE -> {
				BiFunction<ImmutableSession, C, SessionActivationNotifier> passivationNotifierFactory = (session, context) -> new ImmutableSessionActivationNotifier<>(sessionProvider, listenerProvider, session, context);
				return marshalling ? new CoarseSessionAttributesFactory<>(new MarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration), passivationNotifierFactory, detachedPassivationNotifierFactory, infinispan) : new CoarseSessionAttributesFactory<>(new IdentityMarshallerSessionAttributesFactoryConfiguration<>(configuration), passivationNotifierFactory, detachedPassivationNotifierFactory, infinispan);
			}
			default -> throw new IllegalStateException();
		}
	}

	@Override
	public void close() {
		this.schedulerListenerRegistration.close();
		this.scheduler.close();
		this.factory.close();
	}
}
