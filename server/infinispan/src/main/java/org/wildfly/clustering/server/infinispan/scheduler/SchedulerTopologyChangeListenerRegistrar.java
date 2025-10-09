/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
import org.wildfly.clustering.cache.infinispan.embedded.listener.EventListenerRegistrar;

/**
 * Handles cache topology events for a distributed scheduler.
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @param <SE> the schedule task entry type
 * @param <CE> the cancel task entry type
 * @author Paul Ferraro
 */
@Listener
public class SchedulerTopologyChangeListenerRegistrar<K, V, SE, CE> extends EventListenerRegistrar {
	private static final System.Logger LOGGER = System.getLogger(SchedulerTopologyChangeListenerRegistrar.class.getName());

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
		Consumer<CacheStreamFilter<SE>> getScheduleTask();

		/**
		 * Returns the task that cancels entries matching a given filter.
		 * @return the task that cancels entries matching a given filter.
		 */
		Consumer<CacheStreamFilter<CE>> getCancelTask();
	}

	private final AtomicReference<Future<Void>> currentFuture = new AtomicReference<>();
	private final Consumer<CacheStreamFilter<SE>> scheduleTask;
	private final Consumer<CacheStreamFilter<CE>> cancelTask;
	private final Executor executor;

	/**
	 * Creates the topology change listener for a scheduler.
	 * @param configuration the listener configuration
	 */
	public SchedulerTopologyChangeListenerRegistrar(Configuration<K, V, SE, CE> configuration) {
		super(configuration.getCacheConfiguration().getCache());
		this.scheduleTask = configuration.getScheduleTask();
		this.cancelTask = configuration.getCancelTask();
		this.executor = configuration.getCacheConfiguration().getExecutor();
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
					if (future != null) {
						future.cancel(true);
					}
					return CompletableFuture.runAsync(() -> this.cancelTask.accept(CacheStreamFilter.segments(formerlyOwnedSegments)), this.executor);
				}
			}
		} else if (!newSegments.isEmpty()) {
			IntSet newlyOwnedSegments = IntSets.mutableCopyFrom(newSegments);
			newlyOwnedSegments.removeAll(IntSets.from(oldSegments));
			// If we have newly owned segments, then run schedule task
			if (!newlyOwnedSegments.isEmpty()) {
				FutureTask<Void> task = new FutureTask<>(() -> this.scheduleTask.accept(CacheStreamFilter.segments(newlyOwnedSegments)), null);
				Future<Void> future = this.currentFuture.getAndSet(task);
				if (future != null) {
					future.cancel(true);
				}
				this.executor.execute(task);
			}
		}
		return CompletableFuture.completedStage(null);
	}
}
