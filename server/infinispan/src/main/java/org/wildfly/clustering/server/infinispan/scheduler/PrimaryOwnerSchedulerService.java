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
import java.util.function.Function;

import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.retry.Retry;

import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistration;
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
	 * @param <SE> the schedule task entry type
	 * @param <CE> the cancel task entry type
	 */
	public interface Configuration<K, V, SE, CE> extends SchedulerTopologyChangeListenerRegistrar.Configuration<K, V, SE, CE> {
		/**
		 * Returns the name of this scheduler.
		 * @return the name of this scheduler.
		 */
		default String getName() {
			return this.getCacheConfiguration().getName();
		}

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
		default Function<K, CacheContainerGroupMember> getAffinity() {
			return new UnaryGroupMemberAffinity<>(this.getCacheConfiguration().getCache(), this.getCommandDispatcherFactory().getGroup());
		}
	}

	private final String name;
	private final CommandDispatcher<CacheContainerGroupMember, Scheduler<K, V>> dispatcher;
	private final CheckedFunction<Map.Entry<K, V>, CompletionStage<Void>> primaryOwnerSchedule;
	private final CheckedFunction<K, CompletionStage<Void>> primaryOwnerCancel;
	private final CheckedFunction<K, CompletionStage<Boolean>> primaryOwnerContains;
	private final ListenerRegistration listenerRegistration;

	/**
	 * Creates a primary owner scheduler from the specified configuration.
	 * @param <SE> the schedule task entry type
	 * @param <CE> the cancel task entry type
	 * @param configuration the configuration of a primary owner scheduler
	 */
	@SuppressWarnings("removal")
	public <SE, CE> PrimaryOwnerSchedulerService(Configuration<K, V, SE, CE> configuration) {
		super(configuration.getScheduler());
		this.name = configuration.getName();
		this.dispatcher = configuration.getCommandDispatcherFactory().createCommandDispatcher(this.name, configuration.getScheduler(), AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return PrimaryOwnerSchedulerService.class.getClassLoader();
			}
		}));
		Function<K, CacheContainerGroupMember> affinity = configuration.getAffinity();
		Retry retry = Retry.of(this.name, configuration.getCacheConfiguration().getRetryConfig());
		this.primaryOwnerSchedule = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, configuration.getScheduleCommandFactory()));
		this.primaryOwnerCancel = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, CancelCommand::new));
		this.primaryOwnerContains = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, ContainsCommand::new));
		this.listenerRegistration = new SchedulerTopologyChangeListenerRegistrar<>(configuration).register();
	}

	@Override
	public void close() {
		this.listenerRegistration.close();
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
