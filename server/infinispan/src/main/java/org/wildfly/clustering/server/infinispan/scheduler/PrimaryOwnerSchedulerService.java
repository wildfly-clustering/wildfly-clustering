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
import java.util.function.BiFunction;
import java.util.function.Function;

import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.retry.Retry;

import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistrar;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistration;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.server.dispatcher.CommandDispatcher;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.util.MapEntry;

/**
 * Scheduler decorator that schedules/cancels a given object on the primary owner.
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public class PrimaryOwnerSchedulerService<I, M> implements SchedulerService<I, M> {
	private static final System.Logger LOGGER = System.getLogger(PrimaryOwnerSchedulerService.class.getName());

	private final String name;
	private final CommandDispatcher<CacheContainerGroupMember, Scheduler<I, M>> dispatcher;
	private final SchedulerService<I, M> scheduler;
	private final ListenerRegistrar listenerRegistrar;
	private final CheckedFunction<I, CompletionStage<Void>> primaryOwnerSchedule;
	private final CheckedFunction<Map.Entry<I, M>, CompletionStage<Void>> primaryOwnerScheduleWithMetaData;
	private final CheckedFunction<I, CompletionStage<Void>> primaryOwnerCancel;
	private final CheckedFunction<I, CompletionStage<Boolean>> primaryOwnerContains;
	private final AtomicReference<ListenerRegistration> listenerRegistration = new AtomicReference<>();
	private final Runnable startTask;

	/**
	 * Creates a primary owner scheduler from the specified configuration.
	 * @param configuration the configuration of a primary owner scheduler
	 */
	@SuppressWarnings("removal")
	public PrimaryOwnerSchedulerService(PrimaryOwnerSchedulerServiceConfiguration<I, M> configuration) {
		this.name = configuration.getName();
		this.listenerRegistrar = configuration.getListenerRegistrar();
		this.scheduler = configuration.getScheduler();
		this.startTask = configuration.getStartTask();
		this.dispatcher = configuration.getCommandDispatcherFactory().createCommandDispatcher(this.name, this.scheduler, AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return PrimaryOwnerSchedulerService.class.getClassLoader();
			}
		}));
		Function<I, CacheContainerGroupMember> affinity = configuration.getAffinity();
		Retry retry = Retry.of(configuration.getName(), configuration.getRetryConfig());
		BiFunction<I, M, ScheduleCommand<I, M>> scheduleCommandFactory = configuration.getScheduleCommandFactory();
		this.primaryOwnerSchedule = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, ScheduleCommand::new));
		this.primaryOwnerScheduleWithMetaData = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, new Function<>() {
			@Override
			public PrimaryOwnerCommand<I, M, Void> apply(Map.Entry<I, M> entry) {
				return scheduleCommandFactory.apply(entry.getKey(), entry.getValue());
			}
		}));
		this.primaryOwnerCancel = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, CancelCommand::new));
		this.primaryOwnerContains = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, ContainsCommand::new));
	}

	@Override
	public boolean isStarted() {
		return this.scheduler.isStarted();
	}

	@Override
	public void start() {
		LOGGER.log(System.Logger.Level.DEBUG, "Starting primary-owner scheduler for {0}", this.name);
		this.scheduler.start();
		this.listenerRegistration.set(this.listenerRegistrar.register());
		this.startTask.run();
		LOGGER.log(System.Logger.Level.DEBUG, "Started primary-owner scheduler for {0}", this.name);
	}

	@Override
	public void stop() {
		LOGGER.log(System.Logger.Level.DEBUG, "Stopping primary-owner scheduler for {0}", this.name);
		Consumer.close().accept(this.listenerRegistration.getAndSet(null));
		this.scheduler.stop();
		LOGGER.log(System.Logger.Level.DEBUG, "Stopped primary-owner scheduler for {0}", this.name);
	}

	@Override
	public void close() {
		LOGGER.log(System.Logger.Level.DEBUG, "Closing primary-owner scheduler for {0}", this.name);
		this.dispatcher.close();
		this.scheduler.close();
		LOGGER.log(System.Logger.Level.DEBUG, "Closed primary-owner scheduler for {0}", this.name);
	}

	@Override
	public void schedule(I id) {
		try {
			this.primaryOwnerSchedule.apply(id).toCompletableFuture().join();
		} catch (CancellationException e) {
			// Ignore
		} catch (Throwable e) {
			LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void schedule(I id, M metaData) {
		try {
			this.primaryOwnerScheduleWithMetaData.apply(MapEntry.of(id, metaData)).toCompletableFuture().join();
		} catch (CancellationException e) {
			// Ignore
		} catch (Throwable e) {
			LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void cancel(I id) {
		try {
			this.primaryOwnerCancel.apply(id).toCompletableFuture().join();
		} catch (CancellationException e) {
			// Ignore
		} catch (Throwable e) {
			LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public boolean contains(I id) {
		try {
			return this.primaryOwnerContains.apply(id).toCompletableFuture().join();
		} catch (CancellationException e) {
			return false;
		} catch (Throwable e) {
			LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
			return false;
		}
	}

	private static class PrimaryOwnerCommandExecutionFunction<I, M, T, R> implements CheckedFunction<T, CompletionStage<R>> {
		private final CommandDispatcher<CacheContainerGroupMember, Scheduler<I, M>> dispatcher;
		private final Function<I, CacheContainerGroupMember> affinity;
		private final Function<T, PrimaryOwnerCommand<I, M, R>> commandFactory;

		PrimaryOwnerCommandExecutionFunction(CommandDispatcher<CacheContainerGroupMember, Scheduler<I, M>> dispatcher, Function<I, CacheContainerGroupMember> affinity, Function<T, PrimaryOwnerCommand<I, M, R>> commandFactory) {
			this.dispatcher = dispatcher;
			this.affinity = affinity;
			this.commandFactory = commandFactory;
		}

		@Override
		public CompletionStage<R> apply(T value) throws IOException {
			PrimaryOwnerCommand<I, M, R> command = this.commandFactory.apply(value);
			CacheContainerGroupMember primaryOwner = this.affinity.apply(command.getId());
			LOGGER.log(System.Logger.Level.TRACE, "Executing command {0} on {1}", command, primaryOwner);
			// This should only go remote following a failover
			return this.dispatcher.dispatchToMember(command, primaryOwner);
		}
	}
}
