/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import java.util.Map;

import org.wildfly.clustering.cache.CacheEntryCreator;
import org.wildfly.clustering.cache.CacheEntryLocator;
import org.wildfly.clustering.cache.CacheEntryRemover;

/**
 * A factory for creating a user context.
 * @param <V> the cache value type
 * @param <PC> the persistent context type
 * @param <TC> the transient context type
 * @author Paul Ferraro
 */
public interface UserContextFactory<V, PC, TC> extends CacheEntryCreator<String, V, PC>, CacheEntryLocator<String, V>, CacheEntryRemover<String> {
	/**
	 * Creates a user context from the specified cache value
	 * @param value the cache value containing the user context
	 * @return a user context from the specified cache value
	 */
	Map.Entry<PC, TC> createUserContext(V value);
}
