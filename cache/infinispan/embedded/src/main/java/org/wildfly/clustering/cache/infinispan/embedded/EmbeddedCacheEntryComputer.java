/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.metadata.EmbeddedMetadata;
import org.infinispan.metadata.Metadata;
import org.wildfly.clustering.cache.infinispan.AbstractCacheEntryMutator;
import org.wildfly.clustering.function.Consumer;

/**
 * Mutator for a cache entry using a compute function.
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 */
public class EmbeddedCacheEntryComputer<K, V> extends AbstractCacheEntryMutator {

	private final Cache<K, V> cache;
	private final K key;
	private final BiFunction<Object, V, V> function;

	EmbeddedCacheEntryComputer(Cache<K, V> cache, K key, BiFunction<Object, V, V> function) {
		this.cache = cache;
		this.key = key;
		this.function = function;
	}

	@Override
	public CompletionStage<Void> mutateAsync() {
		Duration maxIdleDuration = this.get();
		Metadata.Builder builder = new EmbeddedMetadata.Builder();
		if (!maxIdleDuration.isZero()) {
			long seconds = maxIdleDuration.getSeconds();
			// Round to nearest second
			if (maxIdleDuration.getNano() > 0) {
				seconds += 1;
			}
			builder.maxIdle(seconds, TimeUnit.SECONDS);
		}
		// Use FAIL_SILENTLY to prevent mutation from failing locally due to remote exceptions
		return this.cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES, Flag.FAIL_SILENTLY).computeAsync(this.key, this.function, builder.build()).thenAccept(Consumer.empty());
	}
}
