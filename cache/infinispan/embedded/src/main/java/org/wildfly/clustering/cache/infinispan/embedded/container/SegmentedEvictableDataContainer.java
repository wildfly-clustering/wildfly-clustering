/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.Cache;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.commons.util.ByRef;
import org.infinispan.commons.util.ConcatIterator;
import org.infinispan.commons.util.FlattenSpliterator;
import org.infinispan.commons.util.IntSet;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.impl.DefaultSegmentedDataContainer;
import org.infinispan.container.impl.PeekableTouchableCaffeineMap;
import org.infinispan.container.impl.PeekableTouchableContainerMap;
import org.infinispan.container.impl.PeekableTouchableMap;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.factories.impl.BasicComponentRegistry;
import org.infinispan.factories.impl.ComponentRef;
import org.wildfly.clustering.cache.caffeine.CacheConfiguration;
import org.wildfly.clustering.cache.caffeine.CacheFactory;
import org.wildfly.clustering.function.Supplier;

/**
 * Copy of {@link org.infinispan.container.impl.BoundedSegmentedDataContainer} with support for selective and time-based eviction.
 * Unfortunately, due to inflexible constructors and package protected fields, we cannot simply extend {@link org.infinispan.container.impl.BoundedSegmentedDataContainer}.
 * @author Paul Ferraro
 * @param <K> the container key type
 * @param <V> the container value type
 */
public class SegmentedEvictableDataContainer<K, V> extends DefaultSegmentedDataContainer<K, V> {

	private final Executor executor;
	private final Cache<K, InternalCacheEntry<K, V>> evictionCache;
	private final PeekableTouchableMap<K, V> entries;

	SegmentedEvictableDataContainer(BasicComponentRegistry registry, Configuration configuration) {
		super(PeekableTouchableContainerMap::new, configuration.clustering().hash().numSegments());
		Map<Object, CompletableFuture<Void>> futures = new ConcurrentHashMap<>();
		BiConsumer<K, InternalCacheEntry<K, V>> evictionListener = (key, entry) -> {
			if (this.passivator.isRunning()) {
				// Schedule an eviction to happen after the key lock is released
				CompletableFuture<Void> future = new CompletableFuture<>();
				futures.put(key, future);
				this.handleEviction(entry, future);
			}
			this.computeEntryRemoved(this.getSegmentForKey(key), key, entry);
		};
		BiConsumer<K, InternalCacheEntry<K, V>> removalListener = (key, entry) -> {
			// It is very important that the fact that this method is invoked AFTER the entry has been evicted outside of the lock.
			// This way we can see if the entry has been updated concurrently with an eviction properly
			CompletableFuture<Void> future = futures.remove(key);
			if (future != null) {
				future.complete(null);
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
		this.evictionCache = new CacheFactory<K, InternalCacheEntry<K, V>>().apply(builder.whenEvicted(evictionListener).whenRemoved(removalListener).build());
		this.entries = new PeekableTouchableCaffeineMap<>(this.evictionCache);
	}

	void handleEviction(InternalCacheEntry<K, V> entry, CompletableFuture<Void> future) {
		handleEviction(entry, this.orderer, this.passivator.running(), this.evictionManager, this, this.executor, future);
	}

	@Override
	protected void computeEntryWritten(int segment, K key, InternalCacheEntry<K, V> value) {
		ConcurrentMap<K, InternalCacheEntry<K, V>> map = super.getMapForSegment(segment);
		if (map != null) {
			map.put(key, value);
		}
	}

	@Override
	protected void computeEntryRemoved(int segment, K key, InternalCacheEntry<K, V> value) {
		ConcurrentMap<K, InternalCacheEntry<K, V>> map = super.getMapForSegment(segment);
		if (map != null) {
			map.remove(key, value);
		}
	}

	@Override
	protected void putEntryInMap(PeekableTouchableMap<K, V> map, int segment, K key, InternalCacheEntry<K, V> ice) {
		map.compute(key, (k, v) -> {
			this.computeEntryWritten(segment, k, ice);
			return ice;
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	protected InternalCacheEntry<K, V> removeEntryInMap(PeekableTouchableMap<K, V> map, int segment, Object key) {
		ByRef<InternalCacheEntry<K, V>> ref = new ByRef<>(null);
		map.computeIfPresent((K) key, (k, prev) -> {
			this.computeEntryRemoved(segment, k, prev);
			ref.set(prev);
			return null;
		});
		return ref.get();
	}

	@Override
	public PeekableTouchableMap<K, V> getMapForSegment(int segment) {
		// All writes and other ops go directly to the caffeine cache
		return this.entries;
	}

	@Override
	public InternalCacheEntry<K, V> get(Object k) {
		return get(-1, k);
	}

	@Override
	public InternalCacheEntry<K, V> peek(Object k) {
		return peek(-1, k);
	}

	@Override
	public void clear() {
		this.entries.clear();
		for (int i = 0; i < this.maps.length(); ++i) {
			this.clearMapIfPresent(i);
		}
	}

	@Override
	public void clear(IntSet segments) {
		this.clearSegments(segments);
		segments.forEach((IntConsumer) this::clearMapIfPresent);
	}

	private void clearMapIfPresent(int segment) {
		ConcurrentMap<?, ?> map = this.maps.get(segment);
		if (map != null) {
			map.clear();
		}
	}

	@Override
	public Iterator<InternalCacheEntry<K, V>> iteratorIncludingExpired() {
		return this.entries.values().iterator();
	}

	@Override
	public Iterator<InternalCacheEntry<K, V>> iteratorIncludingExpired(IntSet segments) {
		// We could explore a streaming approach here to not have to allocate an additional ArrayList
		List<Collection<InternalCacheEntry<K, V>>> valueIterables = new ArrayList<>(segments.size() + 1);
		PrimitiveIterator.OfInt iter = segments.iterator();
		boolean includeOthers = false;
		while (iter.hasNext()) {
			int segment = iter.nextInt();
			ConcurrentMap<K, InternalCacheEntry<K, V>> map = this.maps.get(segment);
			if (map != null) {
				valueIterables.add(map.values());
			} else {
				includeOthers = true;
			}
		}
		if (includeOthers) {
			valueIterables.add(this.entries.values().stream()
					.filter(e -> segments.contains(getSegmentForKey(e.getKey())))
					.collect(Collectors.toSet()));
		}
		return new ConcatIterator<>(valueIterables);
	}

	@Override
	public Spliterator<InternalCacheEntry<K, V>> spliteratorIncludingExpired() {
		return this.entries.values().spliterator();
	}

	@Override
	public Spliterator<InternalCacheEntry<K, V>> spliteratorIncludingExpired(IntSet segments) {
		// Copy the ints into an array to parallelize them
		int[] segmentArray = segments.toIntArray();
		AtomicBoolean usedOthers = new AtomicBoolean(false);

		return new FlattenSpliterator<>(i -> {
			ConcurrentMap<K, InternalCacheEntry<K, V>> map = this.maps.get(segmentArray[i]);
			if (map == null) {
				if (!usedOthers.getAndSet(true)) {
					return this.entries.values().stream()
							.filter(e -> segments.contains(getSegmentForKey(e.getKey())))
							.collect(Collectors.toSet());
				}
				return Collections.emptyList();
			}
			return map.values();
		}, segmentArray.length, Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.DISTINCT);
	}

	@Override
	public int sizeIncludingExpired() {
		return this.entries.size();
	}

	/**
	 * Clears entries out of caffeine map by invoking remove on iterator. This can either keep all keys that match the
	 * provided segments when keepSegments is <code>true</code> or it will remove only the provided segments when
	 * keepSegments is <code>false</code>.
	 * @param segments the segments to either remove or keep
	 */
	private void clearSegments(IntSet segments) {
		for (Iterator<K> keyIterator = this.entries.keySet().iterator(); keyIterator.hasNext(); ) {
			K key = keyIterator.next();
			int keySegment = getSegmentForKey(key);
			if (segments.contains(keySegment)) {
				keyIterator.remove();
			}
		}
	}

	@Override
	public void removeSegments(IntSet segments) {
		// Call super remove segments so the maps are removed more efficiently
		super.removeSegments(segments);
		// Finally remove the entries from bounded cache
		this.clearSegments(segments);
	}

	@Override
	public long capacity() {
		return this.evictionCache.policy().eviction().get().getMaximum();
	}

	@Override
	public void resize(long newSize) {
		this.evictionCache.policy().eviction().get().setMaximum(newSize);
	}

	@Override
	public long evictionSize() {
		return this.evictionCache.policy().eviction().get().weightedSize().orElse(this.entries.size());
	}

	@Override
	public void cleanUp() {
		this.evictionCache.cleanUp();
	}
}
