/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.container;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.commons.util.concurrent.CompletableFutures;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.impl.AbstractDelegatingInternalDataContainer;
import org.infinispan.container.impl.InternalDataContainer;
import org.infinispan.eviction.EvictionManager;
import org.infinispan.eviction.impl.PassivationManager;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.factories.annotations.Start;
import org.infinispan.factories.annotations.Stop;
import org.infinispan.factories.impl.BasicComponentRegistry;
import org.infinispan.factories.impl.ComponentRef;
import org.infinispan.util.concurrent.DataOperationOrderer;
import org.wildfly.clustering.cache.caffeine.CacheConfiguration;
import org.wildfly.clustering.cache.caffeine.CacheFactory;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;

/**
 * An evictable data container
 * @author Paul Ferraro
 * @param <K> the container key type
 * @param <V> the container value type
 */
public class DefaultEvictableDataContainer<K, V> extends AbstractDelegatingInternalDataContainer<K, V> {
	private static final System.Logger LOGGER = System.getLogger(DefaultEvictableDataContainer.class.getName());

	private final Executor executor;
	private final EvictableDataContainer<K, V> container;
	private final Map<K, InternalCacheEntry<K, V>> passivatingEntries = new ConcurrentHashMap<>();

	DefaultEvictableDataContainer(BasicComponentRegistry registry, Configuration configuration, boolean segmented) {
		Map<Object, CompletableFuture<Void>> evictions = new ConcurrentHashMap<>();
		RemovalListener<K, InternalCacheEntry<K, V>> evictionListener = new RemovalListener<>() {
			@Override
			public void onRemoval(K key, InternalCacheEntry<K, V> entry, RemovalCause cause) {
				// Schedule an eviction to happen after the key lock is released
				CompletableFuture<Void> eviction = new CompletableFuture<>();
				if (evictions.putIfAbsent(key, eviction) == null) {
					DefaultEvictableDataContainer.this.passivatingEntries.put(key, entry);
					DefaultEvictableDataContainer.this.handleEviction(key, entry, eviction).whenComplete((ignore, e) -> DefaultEvictableDataContainer.this.passivatingEntries.remove(key));
				}
			}
		};
		RemovalListener<K, InternalCacheEntry<K, V>> removalListener = new RemovalListener<>() {
			@Override
			public void onRemoval(K key, InternalCacheEntry<K, V> entry, RemovalCause cause) {
				DefaultEvictableDataContainer.this.container.computeEntryRemoved(key, entry);
				CompletableFuture<Void> eviction = evictions.remove(key);
				if (eviction != null) {
					eviction.complete(null);
				}
			}
		};
		Supplier<DataContainerConfigurationBuilder> factory = DataContainerConfigurationBuilder::new;
		DataContainerConfiguration container = Optional.ofNullable(configuration.module(DataContainerConfiguration.class)).orElseGet(factory.thenApply(Builder::create));
		CacheConfiguration.Builder<K, InternalCacheEntry<K, V>> builder = CacheConfiguration.builder();
		if (configuration.memory().maxCount() > 0) {
			builder.withMaxWeight(configuration.memory().maxCount()).evictableWhen(container.evictable());
		}
		Optional.ofNullable(container.idleTimeout()).ifPresent(builder::evictAfter);
		Optional.ofNullable(registry.getComponent(KnownComponentNames.EXPIRATION_SCHEDULED_EXECUTOR, ScheduledExecutorService.class)).map(ComponentRef::running).ifPresent(builder::withExecutor);
		this.executor = registry.getComponent(KnownComponentNames.NON_BLOCKING_EXECUTOR, Executor.class).running();
		Cache<K, InternalCacheEntry<K, V>> evictionCache = new CacheFactory<K, InternalCacheEntry<K, V>>().apply(builder.whenEvicted(evictionListener).whenRemoved(removalListener).build());
		this.container = segmented ? new SegmentedDataContainer<>(configuration, evictionCache, this.executor) : new NonSegmentedDataContainer<>(evictionCache, this.executor);
	}

	/**
	 * Starts the data container delegate.
	 */
	@Start
	public void start() {
		this.container.start();
	}

	/**
	 * Stops the data container delegate.
	 */
	@Stop
	public void stop() {
		this.container.stop();
	}

	@Override
	protected InternalDataContainer<K, V> delegate() {
		return this.container;
	}

	@Override
	public InternalCacheEntry<K, V> peek(Object key) {
		InternalCacheEntry<K, V> entry = this.passivatingEntries.get(key);
		return (entry != null) ? entry : this.container.peek(key);
	}

	@Override
	public InternalCacheEntry<K, V> peek(int segment, Object key) {
		InternalCacheEntry<K, V> entry = this.passivatingEntries.get(key);
		return (entry != null) ? entry : this.container.peek(segment, key);
	}

	CompletionStage<Void> handleEviction(K key, InternalCacheEntry<K, V> entry, CompletableFuture<Void> selfDelay) {
		CompletableFuture<DataOperationOrderer.Operation> future = new CompletableFuture<>();
		DataOperationOrderer orderer = this.container.getOrderer();
		EvictionManager<K, V> evictionManager = this.container.getEvictionManager().orElse(null);
		PassivationManager passivationManager = this.container.getPassivatorManager().orElse(null);
		Function<InternalCacheEntry<K, V>, CompletionStage<Void>> passivator = (passivationManager != null) ? passivationManager::passivateAsync : Function.of(CompletableFutures.completedNull());
		Function<Void, CompletionStage<Void>> notifier = ignore -> {
			CompletionStage<Void> passivate = passivator.apply(entry);
			CompletionStage<Void> result = (evictionManager != null) ? passivate.thenCompose(ignored -> evictionManager.onEntryEviction(Collections.singletonMap(key, entry))) : passivate;
			return (orderer != null) ? result.whenComplete((ignored, e) -> orderer.completeOperation(key, future, DataOperationOrderer.Operation.READ)) : result;
		};
		CompletionStage<DataOperationOrderer.Operation> ordererStage = (orderer != null) ? orderer.orderOn(key, future) : null;
		if (ordererStage != null) {
			LOGGER.log(System.Logger.Level.TRACE, "Encountered concurrent operation during eviction of {0}", key);
			// We have to use thenComposeAsync here in case if the stage is completed between checking and passing
			// this lambda to prevent running the following code while the write lock is held
			return ordererStage.thenComposeAsync(operation -> {
				LOGGER.log(System.Logger.Level.TRACE, "Concurrent operation during eviction of {0} was {1}", key, operation);
				Function<Void, CompletionStage<Void>> skipPassivation = ignore -> {
					LOGGER.log(System.Logger.Level.TRACE, "Skipping passivation for key {0} due to {1}", key, operation);
					orderer.completeOperation(key, future, DataOperationOrderer.Operation.READ);
					return CompletableFutures.completedNull();
				};
				return selfDelay.thenCompose(switch (operation) {
					case REMOVE -> skipPassivation;
					case READ -> notifier;
					case WRITE -> this.containsKey(key) ? skipPassivation : notifier;
				});
			}, this.executor);
		}
		return selfDelay.thenCompose(notifier);
	}
}
