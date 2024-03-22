/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.CacheEntryCreator;
import org.wildfly.clustering.cache.CacheEntryLocator;
import org.wildfly.clustering.cache.CacheEntryRemover;

/**
 * @param <V> the cache value type
 * @param <PC> the persistent context type
 * @param <TC> the transient context type
 * @author Paul Ferraro
 */
public interface UserContextFactory<V, PC, TC> extends CacheEntryCreator<String, V, PC>, CacheEntryLocator<String, V>, CacheEntryRemover<String> {

	Map.Entry<PC, TC> createUserContext(V value);

	default CompletionStage<Map.Entry<PC, TC>> createUserContext(CompletionStage<V> value) {
		return value.thenApply(this::createUserContext);
	}
}
