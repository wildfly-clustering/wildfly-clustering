/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata;

import java.time.Duration;

import org.wildfly.clustering.cache.Creator;
import org.wildfly.clustering.cache.Remover;
import org.wildfly.clustering.server.Registration;

/**
 * Factory for session metadata.
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public interface SessionMetaDataFactory<V> extends ImmutableSessionMetaDataFactory<V>, Creator<String, V, Duration>, Remover<String>, Registration {
	InvalidatableSessionMetaData createSessionMetaData(String id, V value);
}
