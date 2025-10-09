/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.infinispan.Cache;
import org.infinispan.commons.util.IntSet;
import org.infinispan.commons.util.IntSets;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.TopologyChanged;
import org.infinispan.notifications.cachelistener.event.TopologyChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.CacheStreamFilter;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistrar;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistration;
import org.wildfly.clustering.context.DefaultThreadFactory;
import org.wildfly.clustering.function.Consumer;

/**
 * Handles cache topology events for a distributed scheduler.
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @param <SE> the schedule task entry type
 * @param <CE> the cancel task entry type
 * @author Paul Ferraro
 */
public class SchedulerTopologyChangeListenerRegistrar<K, V, SE, CE> implements ListenerRegistrar {
	private static final System.Logger LOGGER = System.getLogger(SchedulerTopologyChangeListenerRegistrar.class.getName());
	@SuppressWarnings("removal")
	private static final ThreadFactory THREAD_FACTORY = new DefaultThreadFactory(SchedulerTopologyChangeListenerRegistrar.class, AccessController.doPrivileged(new PrivilegedAction<>() {
		@Override
		public ClassLoader run() {
			return SchedulerTopologyChangeListenerRegistrar.class.getClassLoader();
		}
	}));

	/**
	 * The configuration of the topology change listener registrar for a scheduler.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @param <SE> the schedule task entry type
	 * @param <CE> the cancel task entry type
	 */
	interface Configuration<K, V, SE, CE> {
		/**
		 * Returns the configuration for the associated cache.
		 * @return the configuration for the associated cache.
		 */
		EmbeddedCacheConfiguration getCacheConfiguration();

		/**
		 * Returns the task that schedule entries matching a given filter.
		 * @return the task that schedule entries matching a given filter.
		 */
		java.util.function.Consumer<CacheStreamFilter<SE>> getScheduleTask();

		/**
		 * Returns the task that cancels entries matching a given filter.
		 * @return the task that cancels entries matching a given filter.
		 */
		java.util.function.Consumer<CacheStreamFilter<CE>> getCancelTask();
	}

	private final Configuration<K, V, SE, CE> configuration;

	/**
	 * Creates the topology change listener for a scheduler.
	 * @param configuration the listener configuration
	 */
	public SchedulerTopologyChangeListenerRegistrar(Configuration<K, V, SE, CE> configuration) {
		this.configuration = configuration;
	}

	@Override
	public ListenerRegistration register() {
		Cache<K, V> cache = this.configuration.getCacheConfiguration().getCache();
		AutoCloseable listener = new TopologyChangedListener<>(this.configuration);
		cache.addListener(listener);
		return () -> {
			cache.removeListener(listener);
			Consumer.close().accept(listener);
		};
	}

	@Listener
	static class TopologyChangedListener<K, V, SE, CE> implements AutoCloseable {
		private final ExecutorService executor = Executors.newSingleThreadExecutor(THREAD_FACTORY);
		private final AtomicReference<Future<Void>> currentFuture = new AtomicReference<>();
		private final java.util.function.Consumer<CacheStreamFilter<SE>> scheduleTask;
		private final java.util.function.Consumer<CacheStreamFilter<CE>> cancelTask;
		private final Duration stopTimeout;

		TopologyChangedListener(Configuration<K, V, SE, CE> configuration) {
			this.scheduleTask = configuration.getScheduleTask();
			this.cancelTask = configuration.getCancelTask();
			this.stopTimeout = configuration.getCacheConfiguration().getStopTimeout();
		}

		/**
		 * Handler for topology changed events.
		 * @param event a topology changed event
		 * @return a completion stage
		 */
		@TopologyChanged
		public CompletionStage<Void> topologyChanged(TopologyChangedEvent<K, V> event) {
			Cache<K, V> cache = event.getCache();
			Address address = cache.getCacheManager().getAddress();
			ConsistentHash oldHash = event.getWriteConsistentHashAtStart();
			Set<Integer> oldSegments = oldHash.getMembers().contains(address) ? oldHash.getPrimarySegmentsForOwner(address) : Collections.emptySet();
			ConsistentHash newHash = event.getWriteConsistentHashAtEnd();
			Set<Integer> newSegments = newHash.getMembers().contains(address) ? newHash.getPrimarySegmentsForOwner(address) : Collections.emptySet();
			LOGGER.log(System.Logger.Level.DEBUG, "{0} scheduler topology change listener received {1}-topology changed event: {2} -> {3}", cache.getName(), event.isPre() ? "pre" : "post", oldHash.getMembers(), newHash.getMembers());
			if (event.isPre()) {
				if (!oldSegments.isEmpty()) {
					IntSet formerlyOwnedSegments = IntSets.mutableCopyFrom(oldSegments);
					formerlyOwnedSegments.removeAll(IntSets.from(newSegments));
					// If there are segments that we no longer own, then run cancellation task
					if (!formerlyOwnedSegments.isEmpty()) {
						Future<Void> future = this.currentFuture.getAndSet(null);
						// Cancel any existing schedule task
						if (future != null) {
							future.cancel(true);
						}
						try {
							LOGGER.log(System.Logger.Level.DEBUG, "{0} cancelling scheduled entries for formerly owned segments: {1}", cache.getName(), formerlyOwnedSegments);
							this.executor.execute(() -> this.cancelTask.accept(CacheStreamFilter.segments(formerlyOwnedSegments)));
						} catch (RejectedExecutionException e) {
							// Ignore
						}
					}
				}
			} else if (!newSegments.isEmpty()) {
				IntSet newlyOwnedSegments = IntSets.mutableCopyFrom(newSegments);
				newlyOwnedSegments.removeAll(IntSets.from(oldSegments));
				// If we have newly owned segments, then run schedule task
				if (!newlyOwnedSegments.isEmpty()) {
					FutureTask<Void> task = new FutureTask<>(() -> this.scheduleTask.accept(CacheStreamFilter.segments(newlyOwnedSegments)), null);
					Future<Void> future = this.currentFuture.getAndSet(task);
					// Cancel any existing schedule task
					if (future != null) {
						future.cancel(true);
					}
					// This will be queued until previous cancel task completes
					try {
						LOGGER.log(System.Logger.Level.DEBUG, "{0} scheduling entries for newly owned segments: {1}", cache.getName(), newlyOwnedSegments);
						this.executor.execute(task);
					} catch (RejectedExecutionException e) {
						// Ignore
					}
				}
			}
			return CompletableFuture.completedStage(null);
		}

		@SuppressWarnings("removal")
		@Override
		public void close() {
			ExecutorService executor = this.executor;
			Duration stopTimeout = this.stopTimeout;
			AccessController.doPrivileged(new PrivilegedAction<Void>() {
				@Override
				public Void run() {
					try {
						executor.shutdownNow();
						executor.awaitTermination(stopTimeout.toMillis(), TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					return null;
				}
			});
		}
	}
}
