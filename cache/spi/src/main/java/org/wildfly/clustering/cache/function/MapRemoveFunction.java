/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.Collections;

/**
 * Function that removes an entry from a map.
 * @author Paul Ferraro
 * @param <K> the map key type
 * @param <V> the map value type
 */
public class MapRemoveFunction<K, V> extends MapComputeFunction<K, V> {

	/**
	 * Constructs a new map remove operation.
	 * @param key a map key
	 */
	public MapRemoveFunction(K key) {
		super(Collections.singletonMap(key, null));
	}
}
