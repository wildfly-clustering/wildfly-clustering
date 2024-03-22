/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata;

import java.time.Duration;

import org.wildfly.clustering.cache.CacheEntryCreator;
import org.wildfly.clustering.cache.CacheEntryRemover;
import org.wildfly.clustering.server.Registration;

/**
 * Factory for session metadata.
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public interface SessionMetaDataFactory<V> extends ImmutableSessionMetaDataFactory<V>, CacheEntryCreator<String, V, Duration>, CacheEntryRemover<String>, Registration {
	InvalidatableSessionMetaData createSessionMetaData(String id, V value);
}
