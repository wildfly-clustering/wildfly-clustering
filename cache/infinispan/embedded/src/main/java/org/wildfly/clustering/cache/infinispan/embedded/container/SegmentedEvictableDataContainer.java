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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

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
import org.infinispan.util.concurrent.WithinThreadExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

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
		Map<K, CompletableFuture<Void>> futures = new ConcurrentHashMap<>();
		RemovalListener<K, InternalCacheEntry<K, V>> evictionListener = new RemovalListener<>() {
			@Override
			public void onRemoval(K key, InternalCacheEntry<K, V> entry, RemovalCause cause) {
				if ((cause == RemovalCause.SIZE) || (cause == RemovalCause.EXPIRED)) {
					// Schedule an eviction to happen after the key lock is released
					CompletableFuture<Void> future = new CompletableFuture<>();
					futures.put(key, future);
					SegmentedEvictableDataContainer.this.handleEviction(entry, future);
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
	protected void computeEntryWritten(K key, InternalCacheEntry<K, V> value) {
		this.computeEntryWritten(this.getSegmentForKey(key), key, value);
	}

	protected void computeEntryWritten(int segment, K key, InternalCacheEntry<K, V> value) {
		Map<K, InternalCacheEntry<K, V>> map = super.getMapForSegment(segment);
		if (map != null) {
			map.put(key, value);
		}
	}

	@Override
	protected void computeEntryRemoved(K key, InternalCacheEntry<K, V> value) {
		this.computeEntryRemoved(this.getSegmentForKey(key), key, value);
	}

	protected void computeEntryRemoved(int segment, K key, InternalCacheEntry<K, V> value) {
		ConcurrentMap<K, InternalCacheEntry<K, V>> map = super.getMapForSegment(segment);
		if (map != null) {
			map.remove(key, value);
		}
	}

	@Override
	protected void putEntryInMap(PeekableTouchableMap<K, V> map, int segment, K key, InternalCacheEntry<K, V> entry) {
		map.compute(key, (k, v) -> {
			this.computeEntryWritten(segment, k, entry);
			return entry;
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
	public InternalCacheEntry<K, V> peek(Object k) {
		return this.peek(-1, k);
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
		this.clear(segments, false);
		IntConsumer clearIfPresent = this::clearMapIfPresent;
		segments.forEach(clearIfPresent);
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
			Map<K, InternalCacheEntry<K, V>> map = this.maps.get(segmentArray[i]);
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
	 * @param keepSegments whether segments are kept or removed
	 */
	private void clear(IntSet segments, boolean keepSegments) {
		for (Iterator<K> keyIterator = this.entries.keySet().iterator(); keyIterator.hasNext(); ) {
			K key = keyIterator.next();
			int keySegment = getSegmentForKey(key);
			if (keepSegments != segments.contains(keySegment)) {
				keyIterator.remove();
			}
		}
	}

	@Override
	public void removeSegments(IntSet segments) {
		// Call super remove segments so the maps are removed more efficiently
		super.removeSegments(segments);
		// Finally remove the entries from bounded cache
		this.clear(segments, false);
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
	public long evictionSize() {
		return this.evictionCache.policy().eviction().orElseThrow().weightedSize().orElse(this.entries.size());
	}

	@Override
	public void cleanUp() {
		this.evictionCache.cleanUp();
	}
}
