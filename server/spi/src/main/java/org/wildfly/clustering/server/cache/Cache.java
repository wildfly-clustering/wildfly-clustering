/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.cache;

import java.util.Set;
import java.util.function.BiFunction;

/**
 * A level-one cache of server-side state.
 * @param <K> the key type
 * @param <V> the value type
 * @author Paul Ferraro
 */
public interface Cache<K, V> {

	/**
	 * Returns the value associated with the specified key from this cache, generating it from the specified function, if necessary.
	 * @param key a key by which the value may be referenced
	 * @param factory a factory for creating the value if it does not already exist within this context.
	 * @return the value, obtained from this context, or generated from the specified factory.
	 */
	V computeIfAbsent(K key, BiFunction<K, Runnable, V> factory);

	/**
	 * Returns the keys of this cache.
	 * @return the keys of this cache.
	 */
	Set<K> keys();
}
