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

import org.jboss.logging.Logger;
import org.wildfly.clustering.server.dispatcher.Command;
import org.wildfly.clustering.server.dispatcher.CommandDispatcher;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.clustering.server.util.Invoker;
import org.wildfly.common.function.ExceptionSupplier;

/**
 * Scheduler decorator that schedules/cancels a given object on the primary owner.
 * @author Paul Ferraro
 */
public class PrimaryOwnerScheduler<I, M> implements Scheduler<I, M>, Function<CompletionStage<Collection<I>>, Stream<I>> {
	private static final Logger LOGGER = org.jboss.logging.Logger.getLogger(PrimaryOwnerScheduler.class);

	private final Function<I, CacheContainerGroupMember> affinity;
	private final CommandDispatcher<CacheContainerGroupMember, CacheEntryScheduler<I, M>> dispatcher;
	private final BiFunction<I, M, ScheduleCommand<I, M>> scheduleCommandFactory;
	private final Invoker invoker;

	public PrimaryOwnerScheduler(PrimaryOwnerSchedulerConfiguration<I, M> configuration) {
		this.scheduleCommandFactory = configuration.getScheduleCommandFactory();
		this.affinity = configuration.getAffinity();
		this.invoker = configuration.getInvoker();
		CacheEntryScheduler<I, M> scheduler = configuration.getScheduler();
		this.dispatcher = configuration.getCommandDispatcherFactory().createCommandDispatcher(configuration.getName(), scheduler, AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> scheduler.getClass().getClassLoader()));
	}

	@Override
	public void schedule(I id, M metaData) {
		try {
			this.executeOnPrimaryOwner(id, this.scheduleCommandFactory.apply(id, metaData));
		} catch (IOException e) {
			LOGGER.warn(id.toString(), e);
		}
	}

	@Override
	public void cancel(I id) {
		try {
			this.executeOnPrimaryOwner(id, new CancelCommand<>(id)).toCompletableFuture().join();
		} catch (IOException | CompletionException e) {
			LOGGER.warn(id.toString(), e);
		} catch (CancellationException e) {
			// Ignore
		}
	}

	@Override
	public boolean contains(I id) {
		try {
			return this.executeOnPrimaryOwner(id, new ContainsCommand<>(id)).toCompletableFuture().join();
		} catch (IOException | CompletionException e) {
			LOGGER.warn(id.toString(), e);
			return false;
		} catch (CancellationException e) {
			return false;
		}
	}

	private <R> CompletionStage<R> executeOnPrimaryOwner(I id, Command<R, CacheEntryScheduler<I, M>, RuntimeException> command) throws IOException {
		Function<I, CacheContainerGroupMember> affinity = this.affinity;
		CommandDispatcher<CacheContainerGroupMember, CacheEntryScheduler<I, M>> dispatcher = this.dispatcher;
		ExceptionSupplier<CompletionStage<R>, IOException> action = new ExceptionSupplier<>() {
			@Override
			public CompletionStage<R> get() throws IOException {
				CacheContainerGroupMember primaryOwner = affinity.apply(id);
				LOGGER.tracef("Executing command %s on %s", command, primaryOwner);
				// This should only go remote following a failover
				return dispatcher.dispatchToMember(command, primaryOwner);
			}
		};
		return this.invoker.invoke(action);
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
		this.dispatcher.close();
		this.dispatcher.getContext().close();
	}
}
