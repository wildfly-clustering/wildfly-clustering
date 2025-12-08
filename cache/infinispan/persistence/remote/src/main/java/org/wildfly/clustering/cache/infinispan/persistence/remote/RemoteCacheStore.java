/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.persistence.remote;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jakarta.transaction.Transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableTransformer;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.infinispan.Cache;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.MetadataValue;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.ServerStatistics;
import org.infinispan.client.hotrod.impl.InternalRemoteCache;
import org.infinispan.client.hotrod.transaction.manager.RemoteTransactionManager;
import org.infinispan.commons.CacheException;
import org.infinispan.commons.configuration.ConfiguredBy;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.util.IntSet;
import org.infinispan.commons.util.IntSets;
import org.infinispan.marshall.persistence.PersistenceMarshaller;
import org.infinispan.metadata.Metadata;
import org.infinispan.persistence.spi.InitializationContext;
import org.infinispan.persistence.spi.MarshallableEntry;
import org.infinispan.persistence.spi.MarshallableEntryFactory;
import org.infinispan.persistence.spi.MarshalledValue;
import org.infinispan.persistence.spi.NonBlockingStore;
import org.infinispan.persistence.spi.PersistenceException;
import org.infinispan.util.concurrent.BlockingManager;
import org.reactivestreams.Publisher;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.SuspendedBatch;
import org.wildfly.clustering.cache.infinispan.batch.TransactionalBatchFactory;
import org.wildfly.clustering.cache.infinispan.remote.ReadForUpdateRemoteCache;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;

/**
 * Alternative to org.infinispan.persistence.remote.RemoteStore configured with a provided {@link RemoteCacheContainer} instance.
 * Other differences include:
 * <ul>
 * <li>Remote caches are auto-created on the remote server.</li>
 * <li>Supports {@link org.infinispan.persistence.spi.NonBlockingStore.Characteristic#TRANSACTIONAL}</li>
 * <li>Supports {@link org.infinispan.persistence.spi.NonBlockingStore.Characteristic#SEGMENTABLE} by using a remote cache per segment allowing for independent segmentation between client and server.</li>
 * </ul>
 *
 * @author Paul Ferraro
 * @param <K> the key type
 * @param <V> the value type
 */
@ConfiguredBy(RemoteCacheStoreConfiguration.class)
public class RemoteCacheStore<K, V> implements NonBlockingStore<K, V> {
	interface Function<T, R> extends org.wildfly.clustering.function.Function<T, R>, io.reactivex.rxjava3.functions.Function<T, R> {
		@Override
		R apply(T value);
	}

	private volatile RemoteCacheContainer container;
	private volatile AtomicReferenceArray<RemoteCache<K, MarshalledValue>> caches;
	private volatile BlockingManager blockingManager;
	private volatile Executor executor;
	private volatile PersistenceMarshaller marshaller;
	private volatile MarshallableEntryFactory<K, V> entryFactory;
	private volatile Function<Map.Entry<K, MetadataValue<MarshalledValue>>, MarshallableEntry<K, V>> entryMapper;
	private volatile int batchSize;
	private volatile String cacheName;
	private volatile int segments;
	private volatile UnaryOperator<InternalRemoteCache<K, MarshalledValue>> cacheTransformer;
	private volatile Supplier<Batch> batchFactory;
	private final Map<Transaction, SuspendedBatch> transactions = new ConcurrentHashMap<>();

	/**
	 * Creates a remote cache store.
	 */
	public RemoteCacheStore() {
	}

	@Override
	public Set<Characteristic> characteristics() {
		// N.B. we must return a new, mutable instance, since this value may be modified by PersistenceManagerImpl
		return EnumSet.of(Characteristic.BULK_READ, Characteristic.EXPIRATION, Characteristic.SEGMENTABLE, Characteristic.SHAREABLE, Characteristic.TRANSACTIONAL);
	}

	@Override
	public CompletionStage<Void> start(InitializationContext context) {
		RemoteCacheStoreConfiguration configuration = context.getConfiguration();
		if (configuration.preload()) {
			throw new IllegalStateException();
		}

		Cache<K, V> cache = context.getCache();
		this.container = configuration.container();
		this.cacheName = cache.getName();
		this.blockingManager = context.getBlockingManager();
		this.executor = context.getNonBlockingExecutor();
		this.batchSize = configuration.maxBatchSize();
		this.marshaller = context.getPersistenceMarshaller();
		this.entryFactory = context.getMarshallableEntryFactory();
		this.entryMapper = entry -> {
			MarshalledValue value = entry.getValue().getValue();
			return this.entryFactory.create(entry.getKey(), value.getValueBytes(), value.getMetadataBytes(), value.getInternalMetadataBytes(), value.getCreated(), value.getLastUsed());
		};
		this.cacheTransformer = configuration.transactional() ? ReadForUpdateRemoteCache::new : UnaryOperator.identity();
		this.segments = configuration.segmented() && (cache.getAdvancedCache().getDistributionManager() != null) ? cache.getCacheConfiguration().clustering().hash().numSegments() : 1;
		this.batchFactory = configuration.transactional() ? new TransactionalBatchFactory(this.cacheName, RemoteTransactionManager.getInstance(), CacheException::new) : null;
		this.caches = new AtomicReferenceArray<>(this.segments);
		for (int i = 0; i < this.segments; ++i) {
			this.container.getConfiguration().addRemoteCache(this.segmentCacheName(i), configuration.andThen(builder -> builder.marshaller(this.marshaller.getUserMarshaller())));
		}
		// When segmented and unshared, add/removeSegments(...) will be triggered as needed.
		return !configuration.segmented() || configuration.shared() ? this.addSegments(IntSets.immutableRangeSet(this.segments)) : CompletableFuture.completedStage(null);
	}

	@Override
	public CompletionStage<Void> stop() {
		CompletableFuture<Void> result = CompletableFuture.completedFuture(null);
		for (int i = 0; i < this.caches.length(); ++i) {
			RemoteCache<K, MarshalledValue> cache = this.caches.get(i);
			if (cache != null) {
				result = CompletableFuture.allOf(result, this.blockingManager.runBlocking(() -> {
					cache.stop();
					cache.getRemoteCacheContainer().getConfiguration().removeRemoteCache(cache.getName());
				}, "hotrod-store-stop").toCompletableFuture());
			}
		}
		return result;
	}

	private String segmentCacheName(int segment) {
		return (this.segments > 1) ? this.cacheName + '.' + segment : this.cacheName;
	}

	private int segmentIndex(int segment) {
		return (this.segments > 1) ? segment : 0;
	}

	private RemoteCache<K, MarshalledValue> segmentCache(int segment) {
		return this.caches.get(this.segmentIndex(segment));
	}

	private PrimitiveIterator.OfInt segmentIterator(IntSet segments) {
		return (this.segments > 1) ? segments.iterator() : IntStream.of(0).iterator();
	}

	@Override
	public CompletionStage<MarshallableEntry<K, V>> load(int segment, Object key) {
		@SuppressWarnings("unchecked")
		K typedKey = (K) key;
		RemoteCache<K, MarshalledValue> cache = this.segmentCache(segment);
		if (cache == null) return CompletableFuture.completedStage(null);
		return cache.getWithMetadataAsync(typedKey).thenApplyAsync(value -> {
			return (value != null) ? this.entryMapper.apply(Map.entry(typedKey, value)) : null;
		}, this.executor);
	}

	@Override
	public CompletionStage<Void> write(int segment, MarshallableEntry<? extends K, ? extends V> entry) {
		RemoteCache<K, MarshalledValue> cache = this.segmentCache(segment);
		if (cache == null) return CompletableFuture.completedStage(null);
		K key = entry.getKey();
		MarshalledValue value = entry.getMarshalledValue();
		Metadata metadata = entry.getMetadata();
		long lifespan = (metadata != null) ? metadata.lifespan() : 0L;
		long maxIdle = (metadata != null) ? metadata.maxIdle() : 0L;
		return cache.withFlags(Flag.SKIP_LISTENER_NOTIFICATION).putAsync(key, value, lifespan, TimeUnit.MILLISECONDS, maxIdle, TimeUnit.MILLISECONDS).thenAcceptAsync(Consumer.empty(), this.executor);
	}

	@Override
	public CompletionStage<Boolean> delete(int segment, Object key) {
		RemoteCache<K, MarshalledValue> cache = this.segmentCache(segment);
		if (cache == null) return CompletableFuture.completedStage(null);
		return cache.withFlags(Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION).removeAsync(key).thenApplyAsync(Objects::nonNull, this.executor);
	}

	private CompletionStage<Void> remove(int segment, Object key) {
		RemoteCache<K, MarshalledValue> cache = this.segmentCache(segment);
		if (cache == null) return CompletableFuture.completedStage(null);
		return cache.withFlags(Flag.SKIP_LISTENER_NOTIFICATION).removeAsync(key).thenAcceptAsync(Consumer.empty(), this.executor);
	}

	@Override
	public CompletionStage<Void> batch(int publisherCount, Publisher<SegmentedPublisher<Object>> removePublisher, Publisher<SegmentedPublisher<MarshallableEntry<K, V>>> writePublisher) {
		// Override default implementation since we do not need result of remove operation
		return this.completableBatch(publisherCount, removePublisher, writePublisher).toCompletionStage(null);
	}

	private Completable completableBatch(int publisherCount, Publisher<SegmentedPublisher<Object>> removePublisher, Publisher<SegmentedPublisher<MarshallableEntry<K, V>>> writePublisher) {
		Completable removeCompletable = Flowable.fromPublisher(removePublisher)
				.flatMap(sp -> Flowable.fromPublisher(sp).map(key -> Map.entry(sp.getSegment(), key)), publisherCount)
				.flatMapCompletable(this::remove, false, this.batchSize);
		Completable writeCompletable = Flowable.fromPublisher(writePublisher)
				.flatMap(sp -> Flowable.fromPublisher(sp).map(entry -> Map.entry(sp.getSegment(), entry)), publisherCount)
				.flatMapCompletable(this::write, false, this.batchSize);
		return removeCompletable.mergeWith(writeCompletable)
				.observeOn(Schedulers.from(this.executor));
	}

	private Completable write(Map.Entry<Integer, MarshallableEntry<K, V>> entry) {
		return Completable.fromCompletionStage(this.write(entry.getKey(), entry.getValue()));
	}

	private Completable remove(Map.Entry<Integer, Object> entry) {
		return Completable.fromCompletionStage(this.remove(entry.getKey(), entry.getValue()));
	}

	@Override
	public Flowable<K> publishKeys(IntSet segments, Predicate<? super K> filter) {
		Stream<K> keys = Stream.empty();
		PrimitiveIterator.OfInt iterator = this.segmentIterator(segments);
		try {
			while (iterator.hasNext()) {
				int segment = iterator.nextInt();
				RemoteCache<K, MarshalledValue> cache = this.segmentCache(segment);
				if (cache != null) {
					keys = Stream.concat(keys, cache.keySet().stream());
				}
			}
			Stream<K> filteredKeys = (filter != null) ? keys.filter(filter) : keys;
			return Flowable.fromStream(filteredKeys).doFinally(filteredKeys::close).observeOn(Schedulers.from(this.executor));
		} catch (PersistenceException e) {
			return Flowable.fromCompletionStage(CompletableFuture.failedStage(e));
		}
	}

	@Override
	public Publisher<MarshallableEntry<K, V>> publishEntries(IntSet segments, Predicate<? super K> filter, boolean includeValues) {
		return includeValues ? this.publishEntries(segments, filter) : this.publishKeys(segments, filter).map(this.entryFactory::create);
	}

	private Flowable<MarshallableEntry<K, V>> publishEntries(IntSet segments, Predicate<? super K> filter) {
		List<Publisher<Map.Entry<K, MetadataValue<MarshalledValue>>>> publishers = new ArrayList<>(segments.size());
		PrimitiveIterator.OfInt iterator = this.segmentIterator(segments);
		try {
			while (iterator.hasNext()) {
				int segment = iterator.nextInt();
				RemoteCache<K, MarshalledValue> cache = this.segmentCache(segment);
				if (cache != null) {
					publishers.add(cache.publishEntriesWithMetadata(null, this.batchSize));
				}
			}
			return !publishers.isEmpty() ? Flowable.concat(publishers).filter(entry -> filter.test(entry.getKey())).map(this.entryMapper).observeOn(Schedulers.from(this.executor)) : Flowable.empty();
		} catch (PersistenceException e) {
			return Flowable.fromCompletionStage(CompletableFuture.failedStage(e));
		}
	}

	@Override
	public CompletionStage<Void> clear() {
		CompletableFuture<Void> result = CompletableFuture.completedFuture(null);
		for (int i = 0; i < this.caches.length(); ++i) {
			RemoteCache<K, MarshalledValue> cache = this.caches.get(i);
			if (cache != null) {
				result = CompletableFuture.allOf(result, cache.withFlags(Flag.SKIP_LISTENER_NOTIFICATION).clearAsync().thenApplyAsync(org.wildfly.clustering.function.Function.identity(), this.executor));
			}
		}
		return result;
	}

	@Override
	public CompletionStage<Boolean> containsKey(int segment, Object key) {
		@SuppressWarnings("unchecked")
		K typedKey = (K) key;
		RemoteCache<K, MarshalledValue> cache = this.segmentCache(segment);
		if (cache == null) return CompletableFuture.completedStage(false);
		try {
			return cache.containsKeyAsync(typedKey).thenApplyAsync(org.wildfly.clustering.function.Function.identity(), this.executor);
		} catch (PersistenceException e) {
			return CompletableFuture.failedStage(e);
		}
	}

	@Override
	public CompletionStage<Boolean> isAvailable() {
		InternalRemoteCache<?, ?> internalCache = (InternalRemoteCache<?, ?>) this.segmentCache(0);
		return internalCache.ping().handleAsync((v, e) -> (e == null) && v.isSuccess(), this.executor);
	}

	@Override
	public CompletionStage<Long> size(IntSet segments) {
		CompletableFuture<Long> result = CompletableFuture.completedFuture(0L);
		PrimitiveIterator.OfInt iterator = this.segmentIterator(segments);
		while (iterator.hasNext()) {
			int segment = iterator.nextInt();
			RemoteCache<K, MarshalledValue> cache = this.caches.get(segment);
			result = result.thenCombineAsync(cache.sizeAsync(), Long::sum, this.executor);
		}
		return result;
	}

	@Override
	public CompletionStage<Long> approximateSize(IntSet segments) {
		CompletableFuture<Long> result = CompletableFuture.completedFuture(0L);
		PrimitiveIterator.OfInt iterator = this.segmentIterator(segments);
		while (iterator.hasNext()) {
			int segment = iterator.nextInt();
			RemoteCache<K, MarshalledValue> cache = this.caches.get(segment);
			result = result.thenCombineAsync(cache.serverStatisticsAsync().thenApply(RemoteCacheStore::approximateEntries), Long::sum, this.executor);
		}
		return result;
	}

	static Long approximateEntries(ServerStatistics stats) {
		return Long.valueOf(stats.getIntStatistic(ServerStatistics.APPROXIMATE_ENTRIES_UNIQUE));
	}

	@Override
	public CompletionStage<Void> addSegments(IntSet segments) {
		CompletableFuture<Void> result = new CompletableFuture<>();
		AtomicInteger count = new AtomicInteger(segments.size());
		PrimitiveIterator.OfInt iterator = segments.iterator();
		while (iterator.hasNext()) {
			int segment = iterator.nextInt();
			String cacheName = this.segmentCacheName(segment);
			int index = this.segmentIndex(segment);
			this.blockingManager.runBlocking(() -> {
				RemoteCache<K, MarshalledValue> cache = this.container.getCache(cacheName);
				cache.start();
				// Entries are opaque to server
				DataFormat format = DataFormat.builder()
						.keyMarshaller(this.marshaller.getUserMarshaller()).keyType(MediaType.APPLICATION_OCTET_STREAM)
						.valueMarshaller(this.marshaller).valueType(MediaType.APPLICATION_OCTET_STREAM)
						.build();
				this.caches.set(index, ((cache instanceof InternalRemoteCache<K, MarshalledValue> internalCache) ? this.cacheTransformer.apply(internalCache) : cache).withDataFormat(format));
			}, "hotrod-store-add-segments").whenComplete((value, e) -> {
				if (e != null) {
					result.completeExceptionally(e);
				} else if (count.decrementAndGet() == 0) {
					result.complete(null);
				}
			});
		}
		return result;
	}

	@Override
	public CompletionStage<Void> removeSegments(IntSet segments) {
		CompletableFuture<Void> result = CompletableFuture.completedFuture(null);
		PrimitiveIterator.OfInt iterator = segments.iterator();
		while (iterator.hasNext()) {
			int segment = iterator.nextInt();
			RemoteCache<K, MarshalledValue> cache = this.caches.get(segment);
			if (cache != null) {
				this.caches.set(segment, null);
				result = CompletableFuture.allOf(result, this.blockingManager.thenRunBlocking(cache.clearAsync().thenAcceptAsync(Consumer.empty(), this.executor), cache::stop, "hotrod-store-remove-segments").toCompletableFuture());
			}
		}
		return result;
	}

	@Override
	public Publisher<MarshallableEntry<K, V>> purgeExpired() {
		return Flowable.empty();
	}

	@Override
	public CompletionStage<Void> prepareWithModifications(Transaction transaction, int publisherCount, Publisher<SegmentedPublisher<Object>> removePublisher, Publisher<SegmentedPublisher<MarshallableEntry<K, V>>> writePublisher) {
		SuspendedBatch suspended = this.transactions.computeIfAbsent(transaction, org.wildfly.clustering.function.Function.of(Consumer.empty(), this.batchFactory.thenApply(Batch::suspend)));
		CompletableTransformer batcher = upstream -> observer -> {
			try (Context<Batch> context = suspended.resumeWithContext()) {
				upstream.subscribe(observer);
			}
		};
		return this.completableBatch(publisherCount, removePublisher, writePublisher).compose(batcher).toCompletionStage(null);
	}

	@Override
	public CompletionStage<Void> commit(Transaction transaction) {
		return this.close(transaction, Consumer.empty(), "hotrod-store-commit");
	}

	@Override
	public CompletionStage<Void> rollback(Transaction transaction) {
		return this.close(transaction, Batch::discard, "hotrod-store-rollback");
	}

	private CompletionStage<Void> close(Transaction transaction, Consumer<Batch> consumer, String operationName) {
		SuspendedBatch suspended = this.transactions.remove(transaction);
		return (suspended != null) ? this.blockingManager.runBlocking(() -> {
			try (Batch batch = suspended.resume()) {
				consumer.accept(batch);
			}
		}, operationName) : CompletableFuture.completedStage(null);
	}
}
