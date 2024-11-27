/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.retry.Retry;

import org.jboss.logging.Logger;
import org.wildfly.clustering.server.dispatcher.CommandDispatcher;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.clustering.server.util.MapEntry;

/**
 * Scheduler decorator that schedules/cancels a given object on the primary owner.
 * @param <I> the identifier type of scheduled entries
 * @param <M> the meta data type
 * @author Paul Ferraro
 */
public class PrimaryOwnerScheduler<I, M> implements Scheduler<I, M>, Function<CompletionStage<Collection<I>>, Stream<I>> {
	private static final Logger LOGGER = Logger.getLogger(PrimaryOwnerScheduler.class);

	private final String name;
	private final CommandDispatcher<CacheContainerGroupMember, CacheEntryScheduler<I, M>> dispatcher;
	private final CheckedFunction<Map.Entry<I, M>, CompletionStage<Void>> primaryOwnerSchedule;
	private final CheckedFunction<I, CompletionStage<Void>> primaryOwnerCancel;
	private final CheckedFunction<I, CompletionStage<Boolean>> primaryOwnerContains;

	public PrimaryOwnerScheduler(PrimaryOwnerSchedulerConfiguration<I, M> configuration) {
		this.name = configuration.getName();
		CacheEntryScheduler<I, M> scheduler = configuration.getScheduler();
		this.dispatcher = configuration.getCommandDispatcherFactory().createCommandDispatcher(this.name, scheduler, AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return scheduler.getClass().getClassLoader();
			}
		}));
		Function<I, CacheContainerGroupMember> affinity = configuration.getAffinity();
		Retry retry = Retry.of(configuration.getName(), configuration.getRetryConfig());
		BiFunction<I, M, ScheduleCommand<I, M>> scheduleCommandFactory = configuration.getScheduleCommandFactory();
		this.primaryOwnerSchedule = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, new Function<>() {
			@Override
			public PrimaryOwnerCommand<I, M, Void> apply(Map.Entry<I, M> entry) {
				return scheduleCommandFactory.apply(entry.getKey(), entry.getValue());
			}
		}));
		this.primaryOwnerCancel = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, CancelCommand::new));
		this.primaryOwnerContains = Retry.decorateCheckedFunction(retry, new PrimaryOwnerCommandExecutionFunction<>(this.dispatcher, affinity, ContainsCommand::new));
	}

	@Override
	public void schedule(I id, M metaData) {
		try {
			this.primaryOwnerSchedule.apply(MapEntry.of(id, metaData)).toCompletableFuture().join();
		} catch (CancellationException e) {
			// Ignore
		} catch (Throwable e) {
			LOGGER.warn(id.toString(), e);
		}
	}

	@Override
	public void cancel(I id) {
		try {
			this.primaryOwnerCancel.apply(id).toCompletableFuture().join();
		} catch (CancellationException e) {
			// Ignore
		} catch (Throwable e) {
			LOGGER.warn(id.toString(), e);
		}
	}

	@Override
	public boolean contains(I id) {
		try {
			return this.primaryOwnerContains.apply(id).toCompletableFuture().join();
		} catch (CancellationException e) {
			return false;
		} catch (Throwable e) {
			LOGGER.warn(id.toString(), e);
			return false;
		}
	}

	@Override
	public Stream<I> stream() {
		try {
			Map<CacheContainerGroupMember, CompletionStage<Collection<I>>> results = this.dispatcher.dispatchToGroup(new EntriesCommand<>());
			return results.isEmpty() ? Stream.empty() : results.values().stream().map(this).flatMap(Function.identity()).distinct();
		} catch (IOException e) {
			return Stream.empty();
		}
	}

	@Override
	public Stream<I> apply(CompletionStage<Collection<I>> stage) {
		try {
			return stage.toCompletableFuture().join().stream();
		} catch (CompletionException e) {
			LOGGER.warn(e.getLocalizedMessage(), e);
			return Stream.empty();
		} catch (CancellationException e) {
			return Stream.empty();
		}
	}

	@Override
	public void close() {
		LOGGER.tracef("Closing command dispatcher for %s primary-owner scheduler", this.name);
		this.dispatcher.close();
		this.dispatcher.getContext().close();
	}

	private static class PrimaryOwnerCommandExecutionFunction<I, M, T, R> implements CheckedFunction<T, CompletionStage<R>> {
		private final CommandDispatcher<CacheContainerGroupMember, CacheEntryScheduler<I, M>> dispatcher;
		private final Function<I, CacheContainerGroupMember> affinity;
		private final Function<T, PrimaryOwnerCommand<I, M, R>> commandFactory;

		PrimaryOwnerCommandExecutionFunction(CommandDispatcher<CacheContainerGroupMember, CacheEntryScheduler<I, M>> dispatcher, Function<I, CacheContainerGroupMember> affinity, Function<T, PrimaryOwnerCommand<I, M, R>> commandFactory) {
			this.dispatcher = dispatcher;
			this.affinity = affinity;
			this.commandFactory = commandFactory;
		}

		@Override
		public CompletionStage<R> apply(T value) throws IOException {
			PrimaryOwnerCommand<I, M, R> command = this.commandFactory.apply(value);
			CacheContainerGroupMember primaryOwner = this.affinity.apply(command.getId());
			LOGGER.tracef("Executing command %s on %s", command, primaryOwner);
			// This should only go remote following a failover
			return this.dispatcher.dispatchToMember(command, primaryOwner);
		}
	}
}
