/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.CacheStreamFilter;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistrar;
import org.wildfly.clustering.context.DefaultThreadFactory;
import org.wildfly.clustering.function.BiFunction;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.cache.CacheStrategy;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.dispatcher.CacheContainerCommandDispatcherFactory;
import org.wildfly.clustering.server.infinispan.expiration.ScheduleWithExpirationMetaDataCommand;
import org.wildfly.clustering.server.infinispan.manager.AffinityIdentifierFactoryService;
import org.wildfly.clustering.server.infinispan.scheduler.CacheEntriesTask;
import org.wildfly.clustering.server.infinispan.scheduler.CacheEntrySchedulerService;
import org.wildfly.clustering.server.infinispan.scheduler.CacheKeysTask;
import org.wildfly.clustering.server.infinispan.scheduler.PrimaryOwnerSchedulerService;
import org.wildfly.clustering.server.infinispan.scheduler.PrimaryOwnerSchedulerServiceConfiguration;
import org.wildfly.clustering.server.infinispan.scheduler.ScheduleCommand;
import org.wildfly.clustering.server.infinispan.scheduler.ScheduleWithTransientMetaDataCommand;
import org.wildfly.clustering.server.infinispan.scheduler.SchedulerService;
import org.wildfly.clustering.server.infinispan.scheduler.SchedulerTopologyChangeListenerRegistrar;
import org.wildfly.clustering.server.local.scheduler.LocalSchedulerService;
import org.wildfly.clustering.server.local.scheduler.LocalSchedulerServiceConfiguration;
import org.wildfly.clustering.server.manager.IdentifierFactoryService;
import org.wildfly.clustering.server.scheduler.Scheduler;
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
	private static final System.Logger LOGGER = System.getLogger(InfinispanSessionManagerFactory.class.getName());
	@SuppressWarnings("removal")
	private static final ThreadFactory THREAD_FACTORY = new DefaultThreadFactory(InfinispanSessionManagerFactory.class, AccessController.doPrivileged(new PrivilegedAction<>() {
		@Override
		public ClassLoader run() {
			return InfinispanSessionManagerFactory.class.getClassLoader();
		}
	}));

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
		Predicate<String> removeTask = new SessionRemoveTask(infinispan.getBatchFactory(), remover);
		@SuppressWarnings("resource")
		org.wildfly.clustering.server.scheduler.SchedulerService<String, Instant> localScheduler = new LocalSchedulerService<>(new LocalSchedulerServiceConfiguration<String>() {
			@Override
			public String getName() {
				return cache.getName();
			}

			@Override
			public Predicate<String> getTask() {
				return removeTask;
			}

			@Override
			public Duration getCloseTimeout() {
				return Duration.ofMillis(cache.getCacheConfiguration().transaction().cacheStopTimeout());
			}

			@Override
			public ThreadFactory getThreadFactory() {
				return THREAD_FACTORY;
			}
		});
		BiFunction<String, ContextualSessionMetaDataEntry<SC>, ExpirationMetaData> metaData = (id, value) -> Optional.of(metaDataFactory.createImmutableSessionMetaData(id, value)).filter(Predicate.not(ExpirationMetaData::isImmortal)).orElse(null);
		CacheEntrySchedulerService<String, SessionMetaDataKey, ContextualSessionMetaDataEntry<SC>, ExpirationMetaData> cacheEntryScheduler = new CacheEntrySchedulerService<>(localScheduler.compose(Function.identity(), ExpirationMetaData::getExpirationTime), metaDataFactory::findValue, metaData);
		CacheContainerCommandDispatcherFactory dispatcherFactory = infinispan.getCommandDispatcherFactory();
		CacheContainerGroup group = dispatcherFactory.getGroup();
		Consumer<CacheStreamFilter<Map.Entry<SessionMetaDataKey, ContextualSessionMetaDataEntry<SC>>>> scheduleTask = CacheEntriesTask.schedule(cache, SessionCacheEntryFilter.META_DATA.cast(), cacheEntryScheduler);
		Consumer<CacheStreamFilter<SessionMetaDataKey>> cancelTask = CacheKeysTask.cancel(cache, SessionCacheKeyFilter.META_DATA, cacheEntryScheduler);
		Runnable startTask = () -> scheduleTask.accept(CacheStreamFilter.local(cache));
		ListenerRegistrar listenerRegistrar = new SchedulerTopologyChangeListenerRegistrar<>(cache, scheduleTask, cancelTask);
		this.scheduler = group.isSingleton() ? cacheEntryScheduler : new PrimaryOwnerSchedulerService<>(new PrimaryOwnerSchedulerServiceConfiguration<String, ExpirationMetaData>() {
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
				return infinispan.getCache();
			}

			@Override
			public BiFunction<String, ExpirationMetaData, ScheduleCommand<String, ExpirationMetaData>> getScheduleCommandFactory() {
				return properties.isTransactional() ? ScheduleWithExpirationMetaDataCommand::new : ScheduleWithTransientMetaDataCommand::new;
			}

			@Override
			public ListenerRegistrar getListenerRegistrar() {
				return listenerRegistrar;
			}

			@Override
			public Runnable getStartTask() {
				return startTask;
			}
		});
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
		this.scheduler.close();
		this.factory.close();
	}

	private static class SessionRemoveTask implements Predicate<String> {
		private final Supplier<Batch> batchFactory;
		private final Predicate<String> remover;

		SessionRemoveTask(Supplier<Batch> batchFactory, Predicate<String> remover) {
			this.batchFactory = batchFactory;
			this.remover = remover;
		}

		@Override
		public boolean test(String id) {
			LOGGER.log(System.Logger.Level.DEBUG, "Expiring session {0}", id);
			try (Batch batch = this.batchFactory.get()) {
				try {
					return this.remover.test(id);
				} catch (RuntimeException | Error e) {
					batch.discard();
					throw e;
				}
			} catch (RuntimeException | Error e) {
				LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
				return false;
			}
		}
	}
}
