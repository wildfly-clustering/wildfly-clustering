/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.infinispan.commons.util.concurrent.CompletableFutures;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.impl.AbstractDelegatingInternalDataContainer;
import org.infinispan.eviction.EvictionManager;
import org.infinispan.eviction.impl.PassivationManager;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.factories.annotations.Start;
import org.infinispan.factories.annotations.Stop;
import org.infinispan.factories.impl.BasicComponentRegistry;
import org.infinispan.persistence.manager.PassivationPersistenceManager;
import org.infinispan.persistence.manager.PersistenceManager;
import org.infinispan.persistence.spi.MarshallableEntry;
import org.infinispan.persistence.spi.MarshallableEntryFactory;
import org.infinispan.util.concurrent.DataOperationOrderer;
import org.infinispan.util.concurrent.WithinThreadExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.RemovalListener;

/**
 * An evictable data container that delegates to a segmented or non-segmented container implementation.
 * @author Paul Ferraro
 * @param <K> the container key type
 * @param <V> the container value type
 */
public class DefaultEvictableDataContainer<K, V> extends AbstractDelegatingInternalDataContainer<K, V> {
	private static final System.Logger LOGGER = System.getLogger(DefaultEvictableDataContainer.class.getName());

	@Inject
	PersistenceManager persistenceManager;
	@Inject
	MarshallableEntryFactory<K, V> persistenceEntryFactory;

	private final EvictableDataContainer<K, V> container;
	private final Executor executor;
	private final boolean allowPeeking;

	private volatile BiConsumer<K, InternalCacheEntry<K, V>> evictionRecorder = (k, v) -> {};

	DefaultEvictableDataContainer(BasicComponentRegistry registry, Configuration configuration, boolean segmented) {
		Map<Object, CompletableFuture<Void>> evictions = new ConcurrentHashMap<>();
		RemovalListener<K, InternalCacheEntry<K, V>> evictionListener = (key, entry, cause) -> {
			LOGGER.log(System.Logger.Level.TRACE, "{0} evicted due to {1}", key, cause);
			if (cause.wasEvicted()) {
				this.evictionRecorder.accept(key, entry);
				this.delegate().evicted(key, entry);
				// Schedule an eviction to happen after the key lock is released
				this.handleEviction(key, entry, evictions.computeIfAbsent(key, k -> new CompletableFuture<>()));
			}
		};
		RemovalListener<K, InternalCacheEntry<K, V>> removalListener = (key, entry, cause) -> {
			LOGGER.log(System.Logger.Level.TRACE, "{0} removed due to {1}", key, cause);
			if (cause.wasEvicted()) {
				CompletableFuture<Void> eviction = evictions.remove(key);
				if (eviction != null) {
					eviction.complete(null);
				}
			}
		};

		DataContainerConfiguration container = Optional.ofNullable(configuration.module(DataContainerConfiguration.class)).orElseGet(() -> new DataContainerConfigurationBuilder().create());
		Cache<K, InternalCacheEntry<K, V>> evictionCache = container.builder(registry, configuration.memory())
				.executor(new WithinThreadExecutor())
				.evictionListener(evictionListener)
				.removalListener(removalListener)
				.build();
		this.allowPeeking = container.attributes().attribute(DataContainerConfiguration.EXPIRY).get() == container.attributes().attribute(DataContainerConfiguration.EXPIRY).getInitialValue();
		this.executor = registry.getComponent(KnownComponentNames.NON_BLOCKING_EXECUTOR, Executor.class).running();
		this.container = segmented ? new SegmentedDataContainer<>(configuration, evictionCache) : new NonSegmentedDataContainer<>(evictionCache);
	}

	/**
	 * Starts the data container delegate.
	 */
	@SuppressWarnings("removal")
	@Start
	public void start() {
		if (this.persistenceManager instanceof PassivationPersistenceManager persistenceManager) {
			MarshallableEntryFactory<K, V> entryFactory = this.persistenceEntryFactory;
			PrivilegedAction<BiConsumer<K, InternalCacheEntry<K, V>>> action = new PrivilegedAction<>() {
				@Override
				public BiConsumer<K, InternalCacheEntry<K, V>> run() {
					// If passivation is enabled, allow recording of evicting entries to PassivationPersistenceManager
					Field field = Stream.of(PassivationPersistenceManager.class.getDeclaredFields()).filter(f -> Map.class.isAssignableFrom(f.getType())).findFirst().get();
					try {
						MethodHandle handle = MethodHandles.privateLookupIn(PassivationPersistenceManager.class, MethodHandles.lookup()).findGetter(field.getDeclaringClass(), field.getName(), field.getType());
						@SuppressWarnings("unchecked")
						Map<K, MarshallableEntry<K, V>> entries = (Map<K, MarshallableEntry<K, V>>) handle.invoke(persistenceManager);
						return (key, value) -> {
							if (value != null) {
								entries.put(key, entryFactory.create(value));
							} else {
								entries.remove(key);
							}
						};
					} catch (Throwable e) {
						throw new IllegalStateException(e);
					}
				}
			};
			this.evictionRecorder = AccessController.doPrivileged(action);
		}
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
	protected EvictableDataContainer<K, V> delegate() {
		return this.container;
	}

	@Override
	public InternalCacheEntry<K, V> peek(Object key) {
		// Discourage spurious asynchronous eviction of an entry during a given cache operation
		return this.allowPeeking ? super.peek(key) : this.get(key);
	}

	@Override
	public InternalCacheEntry<K, V> peek(int segment, Object key) {
		// Discourage spurious asynchronous eviction of an entry during a given cache operation
		return this.allowPeeking ? super.peek(segment, key) : this.get(segment, key);
	}

	CompletionStage<Void> handleEviction(K key, InternalCacheEntry<K, V> entry, CompletableFuture<Void> selfDelay) {
		CompletableFuture<DataOperationOrderer.Operation> future = new CompletableFuture<>();
		DataOperationOrderer orderer = this.container.getOrderer();
		CompletionStage<DataOperationOrderer.Operation> ordererStage = (orderer != null) ? orderer.orderOn(key, future) : null;
		BiConsumer<Void, Throwable> completeOperation = (ignore, e) -> {
			if (orderer != null) {
				orderer.completeOperation(key, future, DataOperationOrderer.Operation.READ);
			}
		};
		Function<Void, CompletionStage<Void>> passivate = ignore -> {
			CompletionStage<Void> result = CompletableFutures.completedNull();
			PassivationManager passivationManager = this.container.getPassivatorManager().orElse(null);
			if (passivationManager != null) {
				LOGGER.log(System.Logger.Level.TRACE, "Passivating evicted entry: {0}", key);
				result = passivationManager.passivateAsync(entry).whenComplete((ignored, e) -> LOGGER.log(System.Logger.Level.TRACE, "Passivated evicted entry: {0}", key));
			}
			EvictionManager<K, V> evictionManager = this.container.getEvictionManager().orElse(null);
			if (evictionManager != null) {
				result = result.thenCompose(ignored -> evictionManager.onEntryEviction(Collections.singletonMap(key, entry)));
			}
			return result;
		};
		if (ordererStage != null) {
			LOGGER.log(System.Logger.Level.TRACE, "Encountered concurrent operation during eviction of {0}", key);
			// We have to use thenComposeAsync here in case if the stage is completed between checking and passing
			// this lambda to prevent running the following code while the write lock is held
			return ordererStage.thenComposeAsync(operation -> {
				LOGGER.log(System.Logger.Level.TRACE, "Concurrent {1} operation during eviction of {0}", key, operation);
				Function<Void, CompletionStage<Void>> skipPassivation = ignore -> {
					LOGGER.log(System.Logger.Level.TRACE, "Skipping passivation of {0} due to {1}", key, operation);
					return CompletableFutures.completedNull();
				};
				return selfDelay.thenCompose(switch (operation) {
					case REMOVE -> skipPassivation;
					case READ -> passivate;
					case WRITE -> this.container.containsKey(key) ? skipPassivation : passivate;
				}).whenComplete(completeOperation);
			}, this.executor);
		}
		return selfDelay.thenCompose(passivate).whenComplete(completeOperation);
	}
}
