/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.distribution;

import java.util.function.UnaryOperator;

import org.infinispan.Cache;
import org.infinispan.CacheStream;
import org.infinispan.commons.util.IntSet;
import org.infinispan.commons.util.IntSets;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.distribution.LocalizedCacheTopology;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.remoting.transport.Address;

/**
 * @author Paul Ferraro
 * @param <T> the stream element type
 */
public interface CacheStreamFilter<T> extends UnaryOperator<CacheStream<T>> {

	/**
	 * Returns a cache stream filter that performs no filtering.
	 * @param <T> the stream element type
	 * @return a cache stream filter
	 */
	static <T> CacheStreamFilter<T> identity() {
		return new CacheStreamFilter<>() {
			@Override
			public CacheStream<T> apply(CacheStream<T> stream) {
				return stream;
			}
		};
	}

	/**
	 * Returns a cache stream filter for the specified segments.
	 * @param segments the segments by which to filter the cache stream.
	 * @param <T> the stream element type
	 * @return a cache stream filter
	 */
	static <T> CacheStreamFilter<T> segments(IntSet segments) {
		return new CacheStreamFilter<>() {
			@Override
			public CacheStream<T> apply(CacheStream<T> stream) {
				return stream.filterKeySegments(segments).disableRehashAware();
			}
		};
	}

	/**
	 * Returns a cache stream filter of the locally owned segments of the specified cache.
	 * @param cache the cache from which to obtain the primary segments.
	 * @param <T> the stream element type
	 * @return a cache stream filter
	 */
	static <T> CacheStreamFilter<T> local(Cache<?, ?> cache) {
		DistributionManager distribution = cache.getAdvancedCache().getDistributionManager();
		LocalizedCacheTopology topology = (distribution != null) ? distribution.getCacheTopology() : null;
		return (topology != null) ? primary(topology.getReadConsistentHash(), topology.getLocalAddress()) : identity();
	}

	/**
	 * Returns a cache stream filter of the segments of the specified consistent hash owned by the specified member.
	 * @param hash a consistent hash
	 * @param member a member of the specified consistent hash
	 * @param <T> the stream element type
	 * @return a cache stream filter
	 */
	static <T> CacheStreamFilter<T> primary(ConsistentHash hash, Address member) {
		return (hash != null) ? segments(IntSets.from(hash.getPrimarySegmentsForOwner(member))) : identity();
	}
}
