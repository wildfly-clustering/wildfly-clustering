/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.container;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;

import com.github.benmanes.caffeine.cache.Cache;

import io.reactivex.rxjava3.core.Flowable;

import org.infinispan.commons.util.FilterIterator;
import org.infinispan.commons.util.FilterSpliterator;
import org.infinispan.commons.util.IntSet;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.impl.AbstractInternalDataContainer;
import org.infinispan.container.impl.PeekableTouchableCaffeineMap;
import org.infinispan.container.impl.PeekableTouchableMap;
import org.infinispan.eviction.EvictionManager;
import org.infinispan.eviction.impl.PassivationManager;
import org.infinispan.util.concurrent.DataOperationOrderer;
import org.reactivestreams.Publisher;

/**
 * A clone of {@link org.infinispan.container.impl.DefaultDataContainer} using a custom caffeine cache.
 * @author Paul Ferraro
 * @param <K> the container key type
 * @param <V> the container value type
 */
public class NonSegmentedDataContainer<K, V> extends AbstractInternalDataContainer<K, V> implements EvictableDataContainer<K, V> {
	private final Cache<K, InternalCacheEntry<K, V>> evictionCache;
	private final PeekableTouchableMap<K, V> entries;

	NonSegmentedDataContainer(Cache<K, InternalCacheEntry<K, V>> evictionCache) {
		this.evictionCache = evictionCache;
		this.entries = new PeekableTouchableCaffeineMap<>(evictionCache);
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public Cache<K, InternalCacheEntry<K, V>> getEvictionCache() {
		return this.evictionCache;
	}

	@Override
	public Map<K, InternalCacheEntry<K, V>> getEntries() {
		return this.entries;
	}

	@Override
	public void evicted(K key, InternalCacheEntry<K, V> entry) {
		// Do nothing
	}

	@Override
	public DataOperationOrderer getOrderer() {
		return this.orderer;
	}

	@Override
	public PassivationManager getPassivatorManager() {
		return this.passivator.running();
	}

	@Override
	public EvictionManager<K, V> getEvictionManager() {
		return this.evictionManager;
	}

	@Override
	protected PeekableTouchableMap<K, V> getMapForSegment(int segment) {
		return this.entries;
	}

	@Override
	protected int getSegmentForKey(Object key) {
		return -1;
	}

	@Override
	public Iterator<InternalCacheEntry<K, V>> iterator() {
		return new EntryIterator(this.iteratorIncludingExpired());
	}

	@Override
	public Spliterator<InternalCacheEntry<K, V>> spliterator() {
		return this.filterExpiredEntries(this.spliteratorIncludingExpired());
	}

	@Override
	public void clear() {
		this.entries.clear();
	}

	@Override
	public void clear(IntSet segments) {
		Iterator<InternalCacheEntry<K, V>> entries = this.iteratorIncludingExpired(segments);
		while (entries.hasNext()) {
			entries.next();
			entries.remove();
		}
	}

	private Predicate<InternalCacheEntry<K, V>> filter(IntSet segments) {
		return entry -> segments.contains(this.keyPartitioner.getSegment(entry.getKey()));
	}

	@Override
	public Spliterator<InternalCacheEntry<K, V>> spliterator(IntSet segments) {
		return new FilterSpliterator<>(this.spliterator(), this.filter(segments));
	}

	@Override
	public Spliterator<InternalCacheEntry<K, V>> spliteratorIncludingExpired(IntSet segments) {
		return new FilterSpliterator<>(this.spliteratorIncludingExpired(), this.filter(segments));
	}

	@Override
	public Iterator<InternalCacheEntry<K, V>> iterator(IntSet segments) {
		return new FilterIterator<>(this.iterator(), this.filter(segments));
	}

	@Override
	public Iterator<InternalCacheEntry<K, V>> iteratorIncludingExpired(IntSet segments) {
		return new FilterIterator<>(this.iteratorIncludingExpired(), this.filter(segments));
	}

	@Override
	public Publisher<InternalCacheEntry<K, V>> publisher(IntSet segments) {
		return Flowable.fromIterable(() -> this.iterator(segments));
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
}
