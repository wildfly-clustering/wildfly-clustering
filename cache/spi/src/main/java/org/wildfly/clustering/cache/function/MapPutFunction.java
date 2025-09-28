/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.Map;

/**
 * Function that puts an entry into a map.
 * @author Paul Ferraro
 * @param <K> the map key type
 * @param <V> the map value type
 */
public class MapPutFunction<K, V> extends MapComputeFunction<K, V> {

	/**
	 * Constructs a new map put operation using the specified key and value.
	 * @param key a map key
	 * @param value a map value
	 */
	public MapPutFunction(K key, V value) {
		super(Map.of(key, value));
	}

	/**
	 * Constructs a new map put operation using the specified entry.
	 * @param entry a map entry
	 */
	MapPutFunction(Map.Entry<K, V> entry) {
		this(entry.getKey(), entry.getValue());
	}
}
