/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.wildfly.clustering.cache.CacheEntryCreator;
import org.wildfly.clustering.cache.CacheEntryRemover;

/**
 * Factory for session metadata.
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public interface SessionMetaDataFactory<V> extends ImmutableSessionMetaDataFactory<V>, CacheEntryCreator<String, V, Map.Entry<Instant, Optional<Duration>>>, CacheEntryRemover<String>, AutoCloseable {
	/**
	 * Creates invalidatable session metadata from the specified identifier and cache value.
	 * @param id the identifier of a session
	 * @param value the cache value of the session
	 * @return invalidatable session metadata from the specified identifier and cache value.
	 */
	InvalidatableSessionMetaData createSessionMetaData(String id, V value);

	@Override
	void close();
}
