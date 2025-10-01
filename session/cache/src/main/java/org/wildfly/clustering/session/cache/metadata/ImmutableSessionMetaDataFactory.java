/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata;

import org.wildfly.clustering.cache.CacheEntryLocator;
import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * Factory for immutable session metadata.
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public interface ImmutableSessionMetaDataFactory<V> extends CacheEntryLocator<String, V> {
	/**
	 * Creates immutable session metadata for the specified identifier and cache entry.
	 * @param id the identifier of a session
	 * @param value the cache value of the session
	 * @return immutable session metadata for the specified identifier and cache entry.
	 */
	ImmutableSessionMetaData createImmutableSessionMetaData(String id, V value);
}
