/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.CacheStreamFilter;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistrar;
import org.wildfly.clustering.context.DefaultThreadFactory;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.cache.CacheStrategy;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.infinispan.dispatcher.CacheContainerCommandDispatcherFactory;
import org.wildfly.clustering.server.infinispan.expiration.ScheduleExpirationCommand;
import org.wildfly.clustering.server.infinispan.manager.AffinityIdentifierFactoryService;
import org.wildfly.clustering.server.infinispan.scheduler.CacheEntriesTask;
import org.wildfly.clustering.server.infinispan.scheduler.CacheEntryScheduler;
import org.wildfly.clustering.server.infinispan.scheduler.CacheEntrySchedulerService;
import org.wildfly.clustering.server.infinispan.scheduler.CacheKeysTask;
import org.wildfly.clustering.server.infinispan.scheduler.PrimaryOwnerCommand;
import org.wildfly.clustering.server.infinispan.scheduler.PrimaryOwnerSchedulerService;
import org.wildfly.clustering.server.infinispan.scheduler.SchedulerTopologyChangeListenerRegistrar;
import org.wildfly.clustering.server.listener.ConsumerRegistry;
import org.wildfly.clustering.server.local.scheduler.LocalSchedulerService;
import org.wildfly.clustering.server.manager.IdentifierFactoryService;
import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.clustering.server.scheduler.SchedulerService;
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

	@SuppressWarnings({ "removal" })
	private static final ThreadFactory THREAD_FACTORY = new DefaultThreadFactory(SessionExpirationTask.class, AccessController.doPrivileged(new PrivilegedAction<>() {
		@Override
		public ClassLoader run() {
			return SessionExpirationTask.class.getClassLoader();
		}
	}));

	/**
	 * The configuration of this session manager factory.
	 * @param <C> the session manager context type
	 * @param <SC> the session context type
	 * @param <S> the session specification type
	 * @param <L> the session event listener specification type
	 */
	public interface Configuration<C, SC, S, L> extends EmbeddedCacheConfiguration {
		/**
		 * Returns the configuration of this session manager factory.
		 * @return the configuration of this session manager factory.
		 */
		SessionManagerFactoryConfiguration<SC> getSessionManagerFactoryConfiguration();

		/**
		 * Returns the session specification provider.
		 * @return the session specification provider.
		 */
		SessionSpecificationProvider<S, C> getSessionSpecificationProvider();

		/**
		 * Returns the session event listener specification provider.
		 * @return the session event listener specification provider.
		 */
		SessionEventListenerSpecificationProvider<S, L> getSessionEventListenerSpecificationProvider();

		/**
		 * Returns a command dispatcher factory.
		 * @return a command dispatcher factory.
		 */
		CacheContainerCommandDispatcherFactory getCommandDispatcherFactory();

		@Override
		default boolean isFaultTolerant() {
			return true;
		}
	}

	private final SchedulerService<String, ExpirationMetaData> scheduler;
	private final SessionFactory<C, ContextualSessionMetaDataEntry<SC>, Object, SC> factory;
	private final EmbeddedCacheConfiguration configuration;
	private final Function<SessionManagerConfiguration<C>, Registrar<SessionManager<SC>>> managerRegistrarFactory;
	private final AtomicInteger managers = new AtomicInteger();

	/**
	 * Creates a session manager factory.
	 * @param <S> the session manager context type
	 * @param <L> the specification type for a session passivation listener
	 * @param configuration the configuration of this factory
	 */
	public <S, L> InfinispanSessionManagerFactory(Configuration<C, SC, S, L> configuration) {
		this.configuration = configuration;
		SessionAttributeActivationNotifierFactory<S, C, L, SC> notifierFactory = new SessionAttributeActivationNotifierFactory<>(configuration.getSessionSpecificationProvider(), configuration.getSessionEventListenerSpecificationProvider());
		SessionMetaDataFactory<ContextualSessionMetaDataEntry<SC>> metaDataFactory = new InfinispanSessionMetaDataFactory<>(configuration);
		@SuppressWarnings("unchecked")
		SessionAttributesFactory<C, Object> attributesFactory = (SessionAttributesFactory<C, Object>) this.createSessionAttributesFactory(configuration, notifierFactory);
		this.factory = (SessionFactory<C, ContextualSessionMetaDataEntry<SC>, Object, SC>) new CompositeSessionFactory<>(metaDataFactory, attributesFactory, configuration.getCacheProperties(), configuration.getSessionManagerFactoryConfiguration().getSessionContextFactory());
		ConsumerRegistry<ImmutableSession> expirationListenerRegistry = ConsumerRegistry.newInstance(CopyOnWriteArrayList::new);
		Predicate<String> expirationTask = new SessionExpirationTask<>(this.factory, configuration.getBatchFactory(), expirationListenerRegistry);
		this.managerRegistrarFactory = new Function<>() {
			@Override
			public Registrar<SessionManager<SC>> apply(SessionManagerConfiguration<C> managerConfiguration) {
				return new Registrar<>() {
					@Override
					public Registration register(SessionManager<SC> manager) {
						Registration contextRegistration = notifierFactory.register(Map.entry(managerConfiguration.getContext(), manager));
						Registration expirationRegistration = expirationListenerRegistry.register(managerConfiguration.getExpirationListener());
						return Registration.composite(List.of(contextRegistration, expirationRegistration));
					}
				};
			}
		};
		Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<SC>> cache = configuration.getCache();
		@SuppressWarnings("resource")
		SchedulerService<String, Instant> localScheduler = new LocalSchedulerService<>(new LocalSchedulerService.Configuration<String>() {
			@Override
			public String getName() {
				return configuration.getName();
			}

			@Override
			public Predicate<String> getTask() {
				return expirationTask;
			}

			@Override
			public Duration getCloseTimeout() {
				return configuration.getStopTimeout();
			}

			@Override
			public ThreadFactory getThreadFactory() {
				return THREAD_FACTORY;
			}
		});
		CacheEntrySchedulerService<String, SessionMetaDataKey, ContextualSessionMetaDataEntry<SC>, ExpirationMetaData> cacheEntryScheduler = new CacheEntrySchedulerService<>(new CacheEntrySchedulerService.Configuration<>() {
			@Override
			public org.wildfly.clustering.server.scheduler.SchedulerService<String, ExpirationMetaData> getSchedulerService() {
				return localScheduler.compose(Function.identity(), ExpirationMetaData::getExpirationTime);
			}

			@Override
			public BiFunction<String, ContextualSessionMetaDataEntry<SC>, ExpirationMetaData> getMetaData() {
				return metaDataFactory::createImmutableSessionMetaData;
			}

			@Override
			public java.util.function.Consumer<CacheEntryScheduler<SessionMetaDataKey, ContextualSessionMetaDataEntry<SC>>> getStartTask() {
				return scheduler -> CacheEntriesTask.schedule(cache, SessionCacheEntryFilter.META_DATA.cast(), scheduler).accept(CacheStreamFilter.local(cache));
			}
		});
		CacheContainerCommandDispatcherFactory dispatcherFactory = configuration.getCommandDispatcherFactory();
		Consumer<CacheStreamFilter<Map.Entry<SessionMetaDataKey, ContextualSessionMetaDataEntry<SC>>>> scheduleTask = CacheEntriesTask.schedule(cache, SessionCacheEntryFilter.META_DATA.cast(), cacheEntryScheduler);
		Consumer<CacheStreamFilter<SessionMetaDataKey>> cancelTask = CacheKeysTask.cancel(cache, SessionCacheKeyFilter.META_DATA, cacheEntryScheduler);
		ListenerRegistrar listenerRegistrar = !dispatcherFactory.getGroup().isSingleton() ? new SchedulerTopologyChangeListenerRegistrar<>(cache, scheduleTask, cancelTask) : null;
		this.scheduler = (listenerRegistrar != null) ? new PrimaryOwnerSchedulerService<>(new PrimaryOwnerSchedulerService.CacheConfiguration<String, ExpirationMetaData>() {
			@Override
			public CacheContainerCommandDispatcherFactory getCommandDispatcherFactory() {
				return dispatcherFactory;
			}

			@Override
			public SchedulerService<String, ExpirationMetaData> getScheduler() {
				return cacheEntryScheduler;
			}

			@Override
			public <K, V> Cache<K, V> getCache() {
				return configuration.getCache();
			}

			@Override
			public Function<Entry<String, ExpirationMetaData>, PrimaryOwnerCommand<String, ExpirationMetaData, Void>> getScheduleCommandFactory() {
				return ScheduleExpirationCommand::new;
			}

			@Override
			public ListenerRegistrar getListenerRegistrar() {
				return listenerRegistrar;
			}
		}) : cacheEntryScheduler;
	}

	@Override
	public SessionManager<SC> createSessionManager(SessionManagerConfiguration<C> configuration) {
		EmbeddedCacheConfiguration cacheConfiguration = this.configuration;
		SessionFactory<C, ContextualSessionMetaDataEntry<SC>, Object, SC> sessionFactory = this.factory;
		IdentifierFactoryService<String> identifierFactory = new AffinityIdentifierFactoryService<>(configuration.getIdentifierFactory(), cacheConfiguration.getCache());
		Registrar<SessionManager<SC>> registrar = this.managerRegistrarFactory.apply(configuration);
		SchedulerService<String, ExpirationMetaData> scheduler = this.scheduler;
		AtomicReference<SessionManager<SC>> reference = new AtomicReference<>();
		BiFunction<String, SC, Session<SC>> detachedSessionFactory = (id, context) -> new DetachedSession<>(reference::getPlain, id, context);
		AtomicInteger managers = this.managers;
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
			public java.util.function.Consumer<ImmutableSession> getExpirationListener() {
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
				// Scheduler is shared between all managers created by this factory
				// Only start the first one
				if (managers.getAndIncrement() == 0) {
					scheduler.start();
				}
			}

			@Override
			public void stop() {
				// Only stop scheduler when last manager stops
				if (managers.decrementAndGet() == 0) {
					scheduler.stop();
				}
				super.stop();
				Consumer.close().accept(this.registration.getAndSet(null));
			}
		};
		reference.setPlain(manager);
		return manager;
	}

	private <S, L> SessionAttributesFactory<C, ?> createSessionAttributesFactory(Configuration<C, SC, S, L> configuration, Function<String, SessionAttributeActivationNotifier> detachedPassivationNotifierFactory) {
		boolean marshalling = configuration.getCacheProperties().isMarshalling();
		switch (configuration.getSessionManagerFactoryConfiguration().getAttributePersistenceStrategy()) {
			case FINE -> {
				BiFunction<ImmutableSession, C, SessionAttributeActivationNotifier> passivationNotifierFactory = (session, context) -> new ImmutableSessionAttributeActivationNotifier<>(configuration.getSessionSpecificationProvider(), configuration.getSessionEventListenerSpecificationProvider(), session, context);
				return marshalling ? new FineSessionAttributesFactory<>(new MarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration.getSessionManagerFactoryConfiguration()), passivationNotifierFactory, detachedPassivationNotifierFactory, configuration) : new FineSessionAttributesFactory<>(new IdentityMarshallerSessionAttributesFactoryConfiguration<>(configuration.getSessionManagerFactoryConfiguration()), passivationNotifierFactory, detachedPassivationNotifierFactory, configuration);
			}
			case COARSE -> {
				BiFunction<ImmutableSession, C, SessionActivationNotifier> passivationNotifierFactory = (session, context) -> new ImmutableSessionActivationNotifier<>(configuration.getSessionSpecificationProvider(), configuration.getSessionEventListenerSpecificationProvider(), session, context);
				return marshalling ? new CoarseSessionAttributesFactory<>(new MarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration.getSessionManagerFactoryConfiguration()), passivationNotifierFactory, detachedPassivationNotifierFactory, configuration) : new CoarseSessionAttributesFactory<>(new IdentityMarshallerSessionAttributesFactoryConfiguration<>(configuration.getSessionManagerFactoryConfiguration()), passivationNotifierFactory, detachedPassivationNotifierFactory, configuration);
			}
			default -> throw new IllegalStateException();
		}
	}

	@Override
	public void close() {
		this.scheduler.close();
		this.factory.close();
	}
}
