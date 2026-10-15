/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.container;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;

import org.infinispan.commons.api.Lifecycle;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.impl.InternalDataContainer;
import org.infinispan.eviction.EvictionManager;
import org.infinispan.eviction.impl.PassivationManager;
import org.infinispan.util.concurrent.DataOperationOrderer;

import com.github.benmanes.caffeine.cache.Cache;

/**
 * An evictable data containers.
 * @author Paul Ferraro
 * @param <K> the container key type
 * @param <V> the container value type
 */
public interface EvictableDataContainer<K, V> extends InternalDataContainer<K, V>, Lifecycle {
	/**
	 * Returns the caffeine cache used for eviction.
	 * @return the caffeine cache used for eviction.
	 */
	Cache<K, InternalCacheEntry<K, V>> getEvictionCache();

	/**
	 * Returns the caffeine cache map.
	 * @return the caffeine cache map.
	 */
	Map<K, InternalCacheEntry<K, V>> getEntries();

	/**
	 * Returns the data operation orderer.
	 * @return the data operation orderer.
	 */
	DataOperationOrderer getOrderer();

	/**
	 * Returns the eviction manager, if present.
	 * @return the eviction manager, if present.
	 */
	Optional<EvictionManager<K, V>> getEvictionManager();

	/**
	 * Returns the passivation manager, if present.
	 * @return the passivation manager, if present.
	 */
	Optional<PassivationManager> getPassivatorManager();

	@Override
	default Iterator<InternalCacheEntry<K, V>> iteratorIncludingExpired() {
		return this.getEntries().values().iterator();
	}

	@Override
	default Spliterator<InternalCacheEntry<K, V>> spliteratorIncludingExpired() {
		// Technically this spliterator is distinct, but it won't be set - we assume that is okay for now
		return this.getEntries().values().spliterator();
	}

	@Override
	default int sizeIncludingExpired() {
		return this.getEntries().size();
	}

	@Override
	default void cleanUp() {
		// Caffeine may not evict an entry right away if concurrent threads are writing, so this forces a cleanUp
		this.getEvictionCache().cleanUp();
	}

	@Override
	default long capacity() {
		return this.getEvictionCache().policy().eviction().get().getMaximum();
	}

	@Override
	default void resize(long newSize) {
		this.getEvictionCache().policy().eviction().get().setMaximum(newSize);
	}

	@Override
	default long evictionSize() {
		return this.getEvictionCache().policy().eviction().get().weightedSize().orElse(this.getEntries().size());
	}

	/**
	 * Indicates entry was evicted from caffeine cache.
	 * @param key the evicted cache key
	 * @param entry the evicted cache entry
	 */
	void evicted(K key, InternalCacheEntry<K, V> entry);
}
