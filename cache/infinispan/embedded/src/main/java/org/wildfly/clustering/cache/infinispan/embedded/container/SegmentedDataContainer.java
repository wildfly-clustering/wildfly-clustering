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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.infinispan.commons.util.ConcatIterator;
import org.infinispan.commons.util.FlattenSpliterator;
import org.infinispan.commons.util.IntSet;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.impl.DefaultSegmentedDataContainer;
import org.infinispan.container.impl.PeekableTouchableCaffeineMap;
import org.infinispan.container.impl.PeekableTouchableContainerMap;
import org.infinispan.container.impl.PeekableTouchableMap;
import org.infinispan.eviction.EvictionManager;
import org.infinispan.eviction.impl.PassivationManager;
import org.infinispan.util.concurrent.DataOperationOrderer;

import com.github.benmanes.caffeine.cache.Cache;

/**
 * A clone of {@link org.infinispan.container.impl.BoundedSegmentedDataContainer} using a custom caffeine cache.
 * @author Paul Ferraro
 * @param <K> the container key type
 * @param <V> the container value type
 */
public class SegmentedDataContainer<K, V> extends DefaultSegmentedDataContainer<K, V> implements EvictableDataContainer<K, V> {
	private final Cache<K, InternalCacheEntry<K, V>> evictionCache;
	private final PeekableTouchableMap<K, V> entries;
	private final Executor executor;

	SegmentedDataContainer(Configuration configuration, Cache<K, InternalCacheEntry<K, V>> evictionCache, Executor executor) {
		super(PeekableTouchableContainerMap::new, configuration.clustering().hash().numSegments());
		this.evictionCache = evictionCache;
		this.entries = new PeekableTouchableCaffeineMap<>(evictionCache);
		this.executor = executor;
	}

	@Override
	public Cache<K, InternalCacheEntry<K, V>> getEvictionCache() {
		return this.evictionCache;
	}

	@Override
	public PeekableTouchableMap<K, V> getEntries() {
		return this.entries;
	}

	@Override
	public DataOperationOrderer getOrderer() {
		return this.orderer;
	}

	@Override
	public Executor getExecutor() {
		return this.executor;
	}

	@Override
	public Optional<PassivationManager> getPassivatorManager() {
		return this.passivator.isRunning() ? Optional.of(this.passivator.running()) : Optional.empty();
	}

	@Override
	public Optional<EvictionManager<K, V>> getEvictionManager() {
		return Optional.ofNullable(this.evictionManager);
	}

	@Override
	protected void computeEntryWritten(K key, InternalCacheEntry<K, V> value) {
		this.computeEntryWritten(this.getSegmentForKey(key), key, value);
	}

	private void computeEntryWritten(int segment, K key, InternalCacheEntry<K, V> value) {
		ConcurrentMap<K, InternalCacheEntry<K, V>> map = super.getMapForSegment(segment);
		if (map != null) {
			map.put(key, value);
		}
	}

	@Override
	public void computeEntryRemoved(K key, InternalCacheEntry<K, V> value) {
		this.computeEntryRemoved(this.getSegmentForKey(key), key, value);
	}

	private void computeEntryRemoved(int segment, K key, InternalCacheEntry<K, V> value) {
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
		AtomicReference<InternalCacheEntry<K, V>> ref = new AtomicReference<>(null);
		map.computeIfPresent((K) key, (k, prev) -> {
			this.computeEntryRemoved(segment, k, prev);
			ref.setPlain(prev);
			return null;
		});
		return ref.getPlain();
	}

	@Override
	public PeekableTouchableMap<K, V> getMapForSegment(int segment) {
		// All writes and other ops go directly to the caffeine cache
		return this.entries;
	}

	@Override
	public InternalCacheEntry<K, V> get(Object key) {
		// Avoid unnecessary segment lookup
		return this.get(-1, key);
	}

	@Override
	public InternalCacheEntry<K, V> peek(Object key) {
		// Avoid unnecessary segment lookup
		return this.peek(-1, key);
	}

	@Override
	public Iterator<InternalCacheEntry<K, V>> iteratorIncludingExpired() {
		return EvictableDataContainer.super.iteratorIncludingExpired();
	}

	@Override
	public Spliterator<InternalCacheEntry<K, V>> spliteratorIncludingExpired() {
		return EvictableDataContainer.super.spliteratorIncludingExpired();
	}

	@Override
	public int sizeIncludingExpired() {
		return EvictableDataContainer.super.sizeIncludingExpired();
	}

	@Override
	public void clear() {
		this.entries.clear();
		super.clear();
	}

	@Override
	public void clear(IntSet segments) {
		this.clearSegments(segments);
		PrimitiveIterator.OfInt iterator = segments.iterator();
		while (iterator.hasNext()) {
			PeekableTouchableMap<K, V> map = super.getMapForSegment(iterator.nextInt());
			if (map != null) {
				map.clear();
			}
		}
	}

	@Override
	public Iterator<InternalCacheEntry<K, V>> iteratorIncludingExpired(IntSet segments) {
		// We could explore a streaming approach here to not have to allocate an additional ArrayList
		List<Collection<InternalCacheEntry<K, V>>> valueIterables = new ArrayList<>(segments.size() + 1);
		PrimitiveIterator.OfInt iter = segments.iterator();
		boolean includeOthers = false;
		while (iter.hasNext()) {
			int segment = iter.nextInt();
			ConcurrentMap<K, InternalCacheEntry<K, V>> map = super.getMapForSegment(segment);
			if (map != null) {
				valueIterables.add(map.values());
			} else {
				includeOthers = true;
			}
		}
		if (includeOthers) {
			valueIterables.add(this.entries.values().stream()
					.filter(e -> segments.contains(this.getSegmentForKey(e.getKey())))
					.collect(Collectors.toSet()));
		}
		return new ConcatIterator<>(valueIterables);
	}

	@Override
	public Spliterator<InternalCacheEntry<K, V>> spliteratorIncludingExpired(IntSet segments) {
		// Copy the ints into an array to parallelize them
		int[] segmentArray = segments.toIntArray();
		AtomicBoolean usedOthers = new AtomicBoolean(false);

		return new FlattenSpliterator<>(i -> {
			Map<K, InternalCacheEntry<K, V>> map = super.getMapForSegment(segmentArray[i]);
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
	public void removeSegments(IntSet segments) {
		// Call super remove segments so the maps are removed more efficiently
		super.removeSegments(segments);
		// Finally remove the entries from bounded cache
		this.clearSegments(segments);
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
			int keySegment = this.getSegmentForKey(key);
			if (segments.contains(keySegment)) {
				keyIterator.remove();
			}
		}
	}
}
