/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.infinispan.Cache;
import org.infinispan.commons.util.IntSet;
import org.infinispan.commons.util.IntSets;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.TopologyChanged;
import org.infinispan.notifications.cachelistener.event.TopologyChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.infinispan.util.concurrent.BlockingManager;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.CacheStreamFilter;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistrar;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistration;
import org.wildfly.clustering.context.DefaultThreadFactory;

/**
 * Handles cache topology events for a distributed scheduler.
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @param <SE> the schedule task entry type
 * @param <CE> the cancel task entry type
 * @author Paul Ferraro
 */
@Listener
public class SchedulerTopologyChangeListenerRegistrar<K, V, SE, CE> implements ListenerRegistrar {
	private static final System.Logger LOGGER = System.getLogger(SchedulerTopologyChangeListenerRegistrar.class.getName());
	@SuppressWarnings("removal")
	private static final ThreadFactory THREAD_FACTORY = new DefaultThreadFactory(SchedulerTopologyChangeListenerRegistrar.class, AccessController.doPrivileged(new PrivilegedAction<>() {
		@Override
		public ClassLoader run() {
			return SchedulerTopologyChangeListenerRegistrar.class.getClassLoader();
		}
	}));

	private final Cache<K, V> cache;
	private final ExecutorService executor = Executors.newSingleThreadExecutor(THREAD_FACTORY);
	private final AtomicReference<Future<?>> scheduleTaskFuture = new AtomicReference<>();
	private final Consumer<CacheStreamFilter<SE>> scheduleTask;
	private final Consumer<CacheStreamFilter<CE>> cancelTask;
	private final BlockingManager blocking;

	/**
	 * Creates the topology change listener for a scheduler.
	 * @param cache an embedded cache
	 * @param scheduleTask a schedule task
	 * @param cancelTask a cancel task
	 */
	public SchedulerTopologyChangeListenerRegistrar(Cache<K, V> cache, Consumer<CacheStreamFilter<SE>> scheduleTask, Consumer<CacheStreamFilter<CE>> cancelTask) {
		this.cache = cache;
		this.scheduleTask = scheduleTask;
		this.cancelTask = cancelTask;
		this.blocking = GlobalComponentRegistry.componentOf(this.cache.getCacheManager(), BlockingManager.class);
	}

	@Override
	public ListenerRegistration register() {
		this.cache.addListener(this);
		return () -> {
			this.cache.removeListener(this);
			LOGGER.log(System.Logger.Level.DEBUG, "Shutting down thread pool for {0} scheduler topology change listener", this.cache.getName());
			this.executor.shutdownNow();
			try {
				LOGGER.log(System.Logger.Level.DEBUG, "Awaiting task termination for {0} scheduler topology change listener", this.cache.getName());
				this.executor.awaitTermination(this.cache.getCacheConfiguration().transaction().cacheStopTimeout(), TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			LOGGER.log(System.Logger.Level.DEBUG, "{0} scheduler topology change listener shutdown complete", this.cache.getName());
		};
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
					Future<?> future = this.scheduleTaskFuture.getAndSet(null);
					if (future != null) {
						future.cancel(true);
					}
					return this.blocking.runBlocking(() -> this.cancelTask.accept(CacheStreamFilter.segments(formerlyOwnedSegments)), this.getClass().getName());
				}
			}
		} else if (!newSegments.isEmpty()) {
			IntSet newlyOwnedSegments = IntSets.mutableCopyFrom(newSegments);
			newlyOwnedSegments.removeAll(IntSets.from(oldSegments));
			// If we have newly owned segments, then run schedule task
			if (!newlyOwnedSegments.isEmpty()) {
				Future<?> future = this.scheduleTaskFuture.getAndSet(this.executor.submit(() -> this.scheduleTask.accept(CacheStreamFilter.segments(newlyOwnedSegments)), this.getClass().getName()));
				if (future != null) {
					future.cancel(true);
				}
			}
		}
		return CompletableFuture.completedStage(null);
	}
}
