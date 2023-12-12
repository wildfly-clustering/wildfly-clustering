/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.context;

import java.util.function.BiFunction;

/**
 * A context for managing references to server-side state.
 * @author Paul Ferraro
 */
public interface Context<K, V> {

	/**
	 * Returns the value associated with the specified key from this context, creating it from the specified factory, if necessary.
	 * @param key a key by which the value may be referenced
	 * @param factory a factory for creating the value if it does not already exist within this context.
	 * @return the value, obtained from this context, or generated from the specified factory.
	 */
	V computeIfAbsent(K key, BiFunction<K, Runnable, V> factory);
}
