/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.container;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.ObjIntConsumer;

import io.reactivex.rxjava3.core.Flowable;

import org.infinispan.commons.util.FilterIterator;
import org.infinispan.commons.util.FilterSpliterator;
import org.infinispan.commons.util.IntSet;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.impl.AbstractInternalDataContainer;
import org.infinispan.container.impl.PeekableTouchableCaffeineMap;
import org.infinispan.container.impl.PeekableTouchableMap;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.factories.annotations.Stop;
import org.infinispan.factories.impl.BasicComponentRegistry;
import org.infinispan.util.concurrent.WithinThreadExecutor;
import org.reactivestreams.Publisher;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

/**
 * Copy of {@link org.infinispan.container.impl.DefaultDataContainer} with support for time-based eviction.
 * Unfortunately, due to inflexible constructors and package protected fields, we cannot simply extend {@link org.infinispan.container.impl.DefaultDataContainer}.
 * @author Paul Ferraro
 * @param <K> the container key type
 * @param <V> the container value type
 */
public class EvictableDataContainer<K, V> extends AbstractInternalDataContainer<K, V> {

	private final Executor executor;
	private final PeekableTouchableMap<K, V> entries;
	private final Cache<K, InternalCacheEntry<K, V>> evictionCache;

	EvictableDataContainer(BasicComponentRegistry registry, Configuration configuration) {
		Map<Object, CompletableFuture<Void>> futures = new ConcurrentHashMap<>();
		RemovalListener<K, InternalCacheEntry<K, V>> evictionListener = new RemovalListener<>() {
			@Override
			public void onRemoval(K key, InternalCacheEntry<K, V> entry, RemovalCause cause) {
				if ((cause == RemovalCause.SIZE) || (cause == RemovalCause.EXPIRED)) {
					// Schedule an eviction to happen after the key lock is released
					CompletableFuture<Void> future = new CompletableFuture<>();
					futures.put(key, future);
					EvictableDataContainer.this.handleEviction(entry, future);
				}
			}
		};
		RemovalListener<K, InternalCacheEntry<K, V>> removalListener = new RemovalListener<>() {
			// It is very important that the fact that this method is invoked AFTER the entry has been evicted outside of the lock.
			// This way we can see if the entry has been updated concurrently with an eviction properly
			@Override
			public void onRemoval(K key, InternalCacheEntry<K, V> value, RemovalCause cause) {
				if ((cause == RemovalCause.SIZE) || (cause == RemovalCause.EXPIRED)) {
					CompletableFuture<Void> future = futures.remove(key);
					if (future != null) {
						future.complete(null);
					}
				}
			}
		};
		DataContainerConfiguration container = Optional.ofNullable(configuration.module(DataContainerConfiguration.class)).orElseGet(() -> new DataContainerConfigurationBuilder().create());
		this.executor = registry.getComponent(KnownComponentNames.NON_BLOCKING_EXECUTOR, Executor.class).running();
		this.evictionCache = container.builder(registry, configuration.memory())
				.executor(new WithinThreadExecutor())
				.evictionListener(evictionListener)
				.removalListener(removalListener)
				.build();

		this.entries = new PeekableTouchableCaffeineMap<>(this.evictionCache);
	}

	void handleEviction(InternalCacheEntry<K, V> entry, CompletableFuture<Void> future) {
		handleEviction(entry, this.orderer, this.passivator.running(), this.evictionManager, this, this.executor, future);
	}

	@Override
	protected PeekableTouchableMap<K, V> getMapForSegment(int segment) {
		return this.entries;
	}

	@Override
	protected int getSegmentForKey(Object key) {
		// We always map to same map, so no reason to waste finding out segment
		return -1;
	}

	@Override
	public int sizeIncludingExpired() {
		return this.entries.size();
	}

	@Override
	public void clear(IntSet segments) {
		Iterator<InternalCacheEntry<K, V>> iter = this.iteratorIncludingExpired(segments);
		while (iter.hasNext()) {
			iter.next();
			iter.remove();
		}
	}

	@Stop
	@Override
	public void clear() {
		this.entries.clear();
	}

	@Override
	public Publisher<InternalCacheEntry<K, V>> publisher(IntSet segments) {
		return Flowable.fromIterable(() -> this.iterator(segments));
	}

	@Override
	public Iterator<InternalCacheEntry<K, V>> iterator() {
		return new EntryIterator(this.entries.values().iterator());
	}

	@Override
	public Iterator<InternalCacheEntry<K, V>> iterator(IntSet segments) {
		return new FilterIterator<>(this.iterator(), entry -> segments.contains(this.keyPartitioner.getSegment(entry.getKey())));
	}

	@Override
	public Spliterator<InternalCacheEntry<K, V>> spliterator() {
		return filterExpiredEntries(this.spliteratorIncludingExpired());
	}

	@Override
	public Spliterator<InternalCacheEntry<K, V>> spliterator(IntSet segments) {
		return new FilterSpliterator<>(this.spliterator(), entry -> segments.contains(this.keyPartitioner.getSegment(entry.getKey())));
	}

	@Override
	public Spliterator<InternalCacheEntry<K, V>> spliteratorIncludingExpired() {
		// Technically this spliterator is distinct, but it won't be set - we assume that is okay for now
		return this.entries.values().spliterator();
	}

	@Override
	public Spliterator<InternalCacheEntry<K, V>> spliteratorIncludingExpired(IntSet segments) {
		return new FilterSpliterator<>(this.spliteratorIncludingExpired(), entry -> segments.contains(this.keyPartitioner.getSegment(entry.getKey())));
	}

	@Override
	public Iterator<InternalCacheEntry<K, V>> iteratorIncludingExpired() {
		return this.entries.values().iterator();
	}

	@Override
	public Iterator<InternalCacheEntry<K, V>> iteratorIncludingExpired(IntSet segments) {
		return new FilterIterator<>(this.iteratorIncludingExpired(), entry -> segments.contains(this.keyPartitioner.getSegment(entry.getKey())));
	}

	@Override
	public void forEachSegment(ObjIntConsumer<PeekableTouchableMap<K, V>> segmentMapConsumer) {
		segmentMapConsumer.accept(this.entries, 0);
	}

	@Override
	public void addSegments(IntSet segments) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeSegments(IntSet segments) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long evictionSize() {
		return this.evictionCache.policy().eviction().orElseThrow().weightedSize().orElse(this.entries.size());
	}

	@Override
	public long capacity() {
		return this.evictionCache.policy().eviction().orElseThrow().getMaximum();
	}

	@Override
	public void resize(long newSize) {
		this.evictionCache.policy().eviction().orElseThrow().setMaximum(newSize);
	}

	@Override
	public void cleanUp() {
		this.evictionCache.cleanUp();
	}
}
