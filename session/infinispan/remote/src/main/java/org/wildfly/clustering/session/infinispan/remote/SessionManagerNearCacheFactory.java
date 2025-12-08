/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.infinispan.client.hotrod.MetadataValue;
import org.infinispan.client.hotrod.configuration.NearCacheConfiguration;
import org.infinispan.client.hotrod.near.NearCache;
import org.infinispan.client.hotrod.near.NearCacheFactory;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.caffeine.CacheConfiguration;
import org.wildfly.clustering.cache.caffeine.CacheFactory;
import org.wildfly.clustering.cache.infinispan.remote.near.CaffeineNearCache;
import org.wildfly.clustering.server.eviction.EvictionConfiguration;
import org.wildfly.clustering.session.infinispan.remote.attributes.SessionAttributesKey;
import org.wildfly.clustering.session.infinispan.remote.metadata.SessionAccessMetaDataKey;
import org.wildfly.clustering.session.infinispan.remote.metadata.SessionCreationMetaDataKey;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

/**
 * A near-cache factory based on max-active-sessions.
 * @author Paul Ferraro
 */
public class SessionManagerNearCacheFactory implements NearCacheFactory {

	private final EvictionConfiguration configuration;

	/**
	 * Creates the near cache factory for this session manager.
	 * @param configuration the eviction configuration
	 */
	public SessionManagerNearCacheFactory(EvictionConfiguration configuration) {
		this.configuration = configuration;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V> NearCache<K, V> createNearCache(NearCacheConfiguration config, BiConsumer<K, MetadataValue<V>> removedConsumer) {
		CacheConfiguration.Builder<K, MetadataValue<V>> builder = CacheConfiguration.builder();
		OptionalInt maxSize = this.configuration.getSizeThreshold();
		Optional<Duration> idleThreshold = this.configuration.getIdleThreshold();
		AtomicReference<Cache<Key<String>, MetadataValue<V>>> reference = new AtomicReference<>();
		if (maxSize.isPresent() || idleThreshold.isPresent()) {
			maxSize.ifPresent(builder::withMaxWeight);
			idleThreshold.ifPresent(builder::evictAfter);
			builder.evictableWhen(SessionCreationMetaDataKey.class::isInstance);
			builder.whenRemoved(new RemovalListener<>() {
				@Override
				public void onRemoval(K key, MetadataValue<V> value, RemovalCause cause) {
					if (cause != RemovalCause.REPLACED) {
						removedConsumer.accept(key, value);

						if ((cause == RemovalCause.EXPIRED) || (cause == RemovalCause.SIZE)) {
							if (key instanceof SessionCreationMetaDataKey creationMetaDataKey) {
								String id = creationMetaDataKey.getId();
								reference.get().invalidateAll(List.of(new SessionAccessMetaDataKey(id), new SessionAttributesKey(id)));
							}
						}
					}
				}
			});
		}
		Cache<K, MetadataValue<V>> cache = new CacheFactory<K, MetadataValue<V>>().apply(builder.build());
		reference.set((Cache<Key<String>, MetadataValue<V>>) cache);
		return new CaffeineNearCache<>(cache);
	}
}
