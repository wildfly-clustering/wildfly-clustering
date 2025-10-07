/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistrar;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistration;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.server.dispatcher.CommandDispatcher;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.infinispan.affinity.UnaryGroupMemberAffinity;
import org.wildfly.clustering.server.infinispan.dispatcher.CacheContainerCommandDispatcherFactory;
import org.wildfly.clustering.server.scheduler.DecoratedSchedulerService;
import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.clustering.server.scheduler.SchedulerService;

/**
 * Scheduler decorator that schedules/cancels a given object on the primary owner.
 * @param <K> the scheduled entry key type
 * @param <V> the scheduled entry value type
 * @author Paul Ferraro
 */
public class PrimaryOwnerSchedulerService<K, V> extends DecoratedSchedulerService<K, V> {
	private static final System.Logger LOGGER = System.getLogger(PrimaryOwnerSchedulerService.class.getName());

	/**
	 * Encapsulates configuration of a {@link PrimaryOwnerSchedulerService}.
	 * @param <K> the scheduled entry key type
	 * @param <V> the scheduled entry value type
	 */
	public interface Configuration<K, V> {
		/**
		 * Returns the name of this scheduler.
		 * @return the name of this scheduler.
		 */
		String getName();

		/**
		 * Returns the delegated scheduler.
		 * @return the delegated scheduler.
		 */
		SchedulerService<K, V> getScheduler();

		/**
		 * Returns the command dispatcher factory for this scheduler.
		 * @return the command dispatcher factory for this scheduler.
		 */
		CacheContainerCommandDispatcherFactory getCommandDispatcherFactory();

		/**
		 * Returns the factory for creating a schedule command.
		 * @return the factory for creating a schedule command.
		 */
		default java.util.function.Function<Map.Entry<K, V>, PrimaryOwnerCommand<K, V, Void>> getScheduleCommandFactory() {
			return ScheduleCommand::new;
		}

		/**
		 * Returns the function returning the group member for which a given identifier has affinity.
		 * @return the function returning the group member for which a given identifier has affinity.
		 */
		Function<K, CacheContainerGroupMember> getAffinity();

		/**
		 * Returns the listener registration for this scheduler.
		 * @return the listener registration for this scheduler.
		 */
		ListenerRegistrar getListenerRegistrar();

		/**
		 * Returns the listener registration for this scheduler.
		 * @return the listener registration for this scheduler.
		 */
		RetryConfig getRetryConfig();
	}

	/**
	 * Encapsulates configuration of a {@link PrimaryOwnerSchedulerService} referencing a cache.
	 * @param <K> the scheduled entry key type
	 * @param <V> the scheduled entry value type
	 */
	public interface CacheConfiguration<K, V> extends Configuration<K, V>, EmbeddedCacheConfiguration {
		@Override
		default String getName() {
			return EmbeddedCacheConfiguration.super.getName();
		}

		@Override
		default RetryConfig getRetryConfig() {
			return EmbeddedCacheConfiguration.super.getRetryConfig();
		}

		/**
		 * Returns the function returning the group member for which a given identifier has affinity.
		 * @return the function returning the group member for which a given identifier has affinity.
		 */
		@Override
		default Function<K, CacheContainerGroupMember> getAffinity() {
			return new UnaryGroupMemberAffinity<>(this.getCache(), this.getCommandDispatcherFactory().getGroup());
		}
	}

	private final String name;
	private final CommandDispatcher<CacheContainerGroupMember, Scheduler<K, V>> dispatcher;
	private final ListenerRegistrar listenerRegistrar;
	private final CheckedFunction<Map.Entry<K, V>, CompletionStage<Void>> primaryOwnerSchedule;
	private final CheckedFunction<K, CompletionStage<Void>> primaryOwnerCancel;
	private final CheckedFunction<K, CompletionStage<Boolean>> primaryOwnerContains;
	private final AtomicReference<ListenerRegistration> listenerRegistration = new AtomicReference<>();

	/**
	 * Creates a primary owner scheduler from the specified configuration.
	 * @param configuration the configuration of a primary owner scheduler
	 */
	@SuppressWarnings("removal")
	public PrimaryOwnerSchedulerService(Configuration<K, V> configuration) {
		super(configuration.getScheduler());
		this.name = configuration.getName();
		this.listenerRegistrar = configuration.getListenerRegistrar();
		this.dispatcher = configuration.getCommandDispatcherFactory().createCommandDispatcher(this.name, configuration.getScheduler(), AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return PrimaryOwnerSchedulerService.class.getClassLoader();
			}
		}));
		Function<K, CacheContainerGroupMember> affinity = configuration.getAffinity();
		Retry retry = Retry.of(configuration.getName(), configuration.getRetryConfig());
		this.primaryOwnerSchedule = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, configuration.getScheduleCommandFactory()));
		this.primaryOwnerCancel = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, CancelCommand::new));
		this.primaryOwnerContains = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, ContainsCommand::new));
	}

	@Override
	public void start() {
		super.start();
		this.listenerRegistration.set(this.listenerRegistrar.register());
	}

	@Override
	public void stop() {
		Consumer.close().accept(this.listenerRegistration.getAndSet(null));
		super.stop();
	}

	@Override
	public void close() {
		this.dispatcher.close();
		super.close();
	}

	@Override
	public void schedule(K id, V metaData) {
		try {
			this.primaryOwnerSchedule.apply(Map.entry(id, metaData)).toCompletableFuture().join();
		} catch (CancellationException e) {
			// Ignore
		} catch (Throwable e) {
			LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void cancel(K id) {
		try {
			this.primaryOwnerCancel.apply(id).toCompletableFuture().join();
		} catch (CancellationException e) {
			// Ignore
		} catch (Throwable e) {
			LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public boolean contains(K id) {
		try {
			return this.primaryOwnerContains.apply(id).toCompletableFuture().join();
		} catch (CancellationException e) {
			return false;
		} catch (Throwable e) {
			LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
			return false;
		}
	}

	private static class PrimaryOwnerCommandExecutionFunction<K, V, T, R> implements CheckedFunction<T, CompletionStage<R>> {
		private final CommandDispatcher<CacheContainerGroupMember, Scheduler<K, V>> dispatcher;
		private final Function<K, CacheContainerGroupMember> affinity;
		private final Function<T, PrimaryOwnerCommand<K, V, R>> commandFactory;

		PrimaryOwnerCommandExecutionFunction(CommandDispatcher<CacheContainerGroupMember, Scheduler<K, V>> dispatcher, Function<K, CacheContainerGroupMember> affinity, Function<T, PrimaryOwnerCommand<K, V, R>> commandFactory) {
			this.dispatcher = dispatcher;
			this.affinity = affinity;
			this.commandFactory = commandFactory;
		}

		@Override
		public CompletionStage<R> apply(T value) throws IOException {
			PrimaryOwnerCommand<K, V, R> command = this.commandFactory.apply(value);
			CacheContainerGroupMember primaryOwner = this.affinity.apply(command.getKey());
			// This should only go remote following a failover
			return this.dispatcher.dispatchToMember(command, primaryOwner);
		}
	}
}
