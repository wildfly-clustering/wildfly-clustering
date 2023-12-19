/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.infinispan.Cache;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.CacheKey;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.Locality;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistration;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshalledValue;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.context.ContextStrategy;
import org.wildfly.clustering.server.dispatcher.CommandDispatcherFactory;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.group.GroupMember;
import org.wildfly.clustering.server.infinispan.affinity.UnaryGroupMemberAffinity;
import org.wildfly.clustering.server.infinispan.expiration.ScheduleWithExpirationMetaDataCommandFactory;
import org.wildfly.clustering.server.infinispan.manager.AffinityIdentifierFactory;
import org.wildfly.clustering.server.infinispan.scheduler.CacheEntryScheduler;
import org.wildfly.clustering.server.infinispan.scheduler.PrimaryOwnerScheduler;
import org.wildfly.clustering.server.infinispan.scheduler.PrimaryOwnerSchedulerConfiguration;
import org.wildfly.clustering.server.infinispan.scheduler.ScheduleCommand;
import org.wildfly.clustering.server.infinispan.scheduler.ScheduleLocalKeysTask;
import org.wildfly.clustering.server.infinispan.scheduler.ScheduleWithTransientMetaDataCommand;
import org.wildfly.clustering.server.infinispan.scheduler.SchedulerTopologyChangeListener;
import org.wildfly.clustering.server.infinispan.util.CacheInvoker;
import org.wildfly.clustering.server.manager.IdentifierFactory;
import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.clustering.server.util.Invoker;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionManagerConfiguration;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.cache.CompositeSessionFactory;
import org.wildfly.clustering.session.cache.ContextualSessionManager;
import org.wildfly.clustering.session.cache.DelegatingSessionManagerConfiguration;
import org.wildfly.clustering.session.cache.SessionFactory;
import org.wildfly.clustering.session.cache.attributes.IdentityMarshallerSessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.MarshalledValueMarshallerSessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;
import org.wildfly.clustering.session.cache.metadata.coarse.ContextualSessionMetaDataEntry;
import org.wildfly.clustering.session.container.ContainerFacadeProvider;
import org.wildfly.clustering.session.infinispan.embedded.attributes.CoarseSessionAttributesFactory;
import org.wildfly.clustering.session.infinispan.embedded.attributes.FineSessionAttributesFactory;
import org.wildfly.clustering.session.infinispan.embedded.attributes.InfinispanSessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.infinispan.embedded.metadata.InfinispanSessionMetaDataFactory;
import org.wildfly.clustering.session.infinispan.embedded.metadata.SessionMetaDataKeyFilter;

/**
 * Factory for creating session managers.
 * @param <S> the HttpSession specification type
 * @param <SC> the ServletContext specification type
 * @param <AL> the HttpSessionAttributeListener specification type
 * @param <LC> the local context type
 * @author Paul Ferraro
 */
public class InfinispanSessionManagerFactory<S, SC, AL, LC> implements SessionManagerFactory<SC, LC, TransactionBatch>, Runnable {
	private final Scheduler<String, ExpirationMetaData> scheduler;
	private final ContainerFacadeProvider<S, SC, AL> provider;
	private final SessionFactory<SC, ContextualSessionMetaDataEntry<LC>, ?, LC> factory;
	private final BiConsumer<Locality, Locality> scheduleTask;
	private final ListenerRegistration schedulerListenerRegistration;
	private final EmbeddedCacheConfiguration configuration;
	private final ExpiredSessionRemover<SC, ?, ?, LC> remover;
	private final SessionAttributeActivationNotifierFactory<S, SC, AL, LC, TransactionBatch> notifierFactory;

	public <GM extends GroupMember<Address>> InfinispanSessionManagerFactory(InfinispanSessionManagerFactoryConfiguration<S, SC, AL, LC, GM> config) {
		this.configuration = config;
		this.provider = config.getContainerFacadeProvider();
		this.notifierFactory = new SessionAttributeActivationNotifierFactory<>(this.provider);
		CacheProperties properties = config.getCacheProperties();
		SessionMetaDataFactory<ContextualSessionMetaDataEntry<LC>> metaDataFactory = new InfinispanSessionMetaDataFactory<>(config);
		this.factory = new CompositeSessionFactory<>(metaDataFactory, this.createSessionAttributesFactory(config), config.getSessionContextFactory());
		this.remover = new ExpiredSessionRemover<>(this.factory);
		Cache<? extends CacheKey<String>, ?> cache = config.getCache();
		CacheEntryScheduler<String, ExpirationMetaData> localScheduler = new SessionExpirationScheduler<>(config.getBatcher(), this.factory.getMetaDataFactory(), this.remover, Duration.ofMillis(cache.getCacheConfiguration().transaction().cacheStopTimeout()));
		GroupCommandDispatcherFactory<Address, GM> dispatcherFactory = config.getCommandDispatcherFactory();
		Group<Address, GM> group = dispatcherFactory.getGroup();
		this.scheduler = group.isSingleton() ? localScheduler : new PrimaryOwnerScheduler<>(new PrimaryOwnerSchedulerConfiguration<String, ExpirationMetaData, GM>() {
			@Override
			public String getName() {
				return cache.getName();
			}

			@Override
			public CommandDispatcherFactory<GM> getCommandDispatcherFactory() {
				return dispatcherFactory;
			}

			@Override
			public CacheEntryScheduler<String, ExpirationMetaData> getScheduler() {
				return localScheduler;
			}

			@Override
			public Function<String, GM> getAffinity() {
				return new UnaryGroupMemberAffinity<>(cache, group);
			}

			@Override
			public BiFunction<String, ExpirationMetaData, ScheduleCommand<String, ExpirationMetaData>> getScheduleCommandFactory() {
				return properties.isTransactional() ? new ScheduleWithExpirationMetaDataCommandFactory<>() : ScheduleWithTransientMetaDataCommand::new;
			}

			@Override
			public Invoker getInvoker() {
				return CacheInvoker.retrying(cache);
			}
		});

		this.scheduleTask = new ScheduleLocalKeysTask<>(cache, SessionMetaDataKeyFilter.INSTANCE, localScheduler);
		this.schedulerListenerRegistration = new SchedulerTopologyChangeListener<>(cache, localScheduler, this.scheduleTask).register();
	}

	@Override
	public void run() {
		this.scheduleTask.accept(Locality.of(false), Locality.forCurrentConsistentHash(this.configuration.getCache()));
	}

	@Override
	public SessionManager<LC, TransactionBatch> createSessionManager(SessionManagerConfiguration<SC, TransactionBatch> configuration) {
		IdentifierFactory<String> identifierFactory = new AffinityIdentifierFactory<>(configuration.getIdentifierFactory(), this.configuration.getCache());
		Registrar<SessionManager<LC, TransactionBatch>> registrar = manager -> {
			Registration contextRegistration = this.notifierFactory.register(Map.entry(configuration.getContext(), manager));
			Registration expirationRegistration = this.remover.register(configuration.getExpirationListener());
			return () -> {
				expirationRegistration.close();
				contextRegistration.close();
			};
		};
		Scheduler<String, ExpirationMetaData> scheduler = this.scheduler;
		InfinispanSessionManagerConfiguration<SC, LC> config = new AbstractInfinispanSessionManagerConfiguration<>(configuration, identifierFactory, this.configuration) {
			@Override
			public Scheduler<String, ExpirationMetaData> getExpirationScheduler() {
				return scheduler;
			}

			@Override
			public Runnable getStartTask() {
				return InfinispanSessionManagerFactory.this;
			}

			@Override
			public Registrar<SessionManager<LC, TransactionBatch>> getRegistrar() {
				return registrar;
			}
		};
		return new ContextualSessionManager<>(new InfinispanSessionManager<>(config, this.factory), this.configuration.getCacheProperties().isTransactional() ? ContextStrategy.UNSHARED : ContextStrategy.SHARED);
	}

	private <GM extends GroupMember<Address>> SessionAttributesFactory<SC, ?> createSessionAttributesFactory(InfinispanSessionManagerFactoryConfiguration<S, SC, AL, LC, GM> configuration) {
		boolean marshalling = configuration.getCacheProperties().isMarshalling();
		switch (configuration.getAttributePersistenceStrategy()) {
			case FINE: {
				return marshalling ? new FineSessionAttributesFactory<>(new InfinispanMarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration, this.notifierFactory)) : new FineSessionAttributesFactory<>(new InfinispanIdentityMarshallerSessionAttributesFactoryConfiguration<>(configuration, this.notifierFactory));
			}
			case COARSE: {
				return marshalling ? new CoarseSessionAttributesFactory<>(new InfinispanMarshalledValueMarshallerSessionAttributesFactoryConfiguration<>(configuration, this.notifierFactory)) : new CoarseSessionAttributesFactory<>(new InfinispanIdentityMarshallerSessionAttributesFactoryConfiguration<>(configuration, this.notifierFactory));
			}
			default: {
				// Impossible
				throw new IllegalStateException();
			}
		}
	}

	@Override
	public void close() {
		this.schedulerListenerRegistration.close();
		this.scheduler.close();
		this.factory.close();
	}

	private abstract static class AbstractInfinispanSessionManagerConfiguration<SC, LC> extends DelegatingSessionManagerConfiguration<SC, TransactionBatch> implements InfinispanSessionManagerConfiguration<SC, LC> {
		private final EmbeddedCacheConfiguration configuration;
		private final IdentifierFactory<String> identifierFactory;

		AbstractInfinispanSessionManagerConfiguration(SessionManagerConfiguration<SC, TransactionBatch> managerConfiguration, IdentifierFactory<String> identifierFactory, EmbeddedCacheConfiguration configuration) {
			super(managerConfiguration);
			this.identifierFactory = identifierFactory;
			this.configuration = configuration;
		}

		@Override
		public IdentifierFactory<String> getIdentifierFactory() {
			return this.identifierFactory;
		}

		@Override
		public <K, V> Cache<K, V> getCache() {
			return this.configuration.getCache();
		}
	}

	private static class InfinispanMarshalledValueMarshallerSessionAttributesFactoryConfiguration<S, SC, AL, V> extends MarshalledValueMarshallerSessionAttributesFactoryConfiguration<S, SC, AL, V> implements InfinispanSessionAttributesFactoryConfiguration<S, SC, AL, V, MarshalledValue<V, ByteBufferMarshaller>> {
		private final EmbeddedCacheConfiguration configuration;
		private final Function<String, SessionAttributeActivationNotifier> notifierFactory;

		<LC, GM extends GroupMember<Address>> InfinispanMarshalledValueMarshallerSessionAttributesFactoryConfiguration(InfinispanSessionManagerFactoryConfiguration<S, SC, AL, LC, GM> configuration, Function<String, SessionAttributeActivationNotifier> notifierFactory) {
			super(configuration);
			this.configuration = configuration;
			this.notifierFactory = notifierFactory;
		}

		@Override
		public Function<String, SessionAttributeActivationNotifier> getActivationNotifierFactory() {
			return this.notifierFactory;
		}

		@Override
		public <KK, VV> Cache<KK, VV> getCache() {
			return this.configuration.getCache();
		}
	}

	private static class InfinispanIdentityMarshallerSessionAttributesFactoryConfiguration<S, SC, AL, V> extends IdentityMarshallerSessionAttributesFactoryConfiguration<S, SC, AL, V> implements InfinispanSessionAttributesFactoryConfiguration<S, SC, AL, V, V> {
		private final EmbeddedCacheConfiguration configuration;
		private final Function<String, SessionAttributeActivationNotifier> notifierFactory;

		<LC, GM extends GroupMember<Address>> InfinispanIdentityMarshallerSessionAttributesFactoryConfiguration(InfinispanSessionManagerFactoryConfiguration<S, SC, AL, LC, GM> configuration, Function<String, SessionAttributeActivationNotifier> notifierFactory) {
			super(configuration);
			this.configuration = configuration;
			this.notifierFactory = notifierFactory;
		}

		@Override
		public Function<String, SessionAttributeActivationNotifier> getActivationNotifierFactory() {
			return this.notifierFactory;
		}

		@Override
		public <KK, VV> Cache<KK, VV> getCache() {
			return this.configuration.getCache();
		}
	}
}
