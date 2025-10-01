/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import java.util.Map;

/**
 * Encapsulates the lifecycle of the attributes of a session.
 * @author Paul Ferraro
 */
public interface SessionAttributes extends Map<String, Object>, AutoCloseable {

	@Override
	default int size() {
		return this.keySet().size();
	}

	@Override
	default boolean isEmpty() {
		return this.keySet().isEmpty();
	}

	@Override
	default boolean containsKey(Object key) {
		return this.keySet().contains(key);
	}

	@Override
	default boolean containsValue(Object value) {
		return this.values().contains(value);
	}

	@Override
	default void putAll(Map<? extends String, ? extends Object> map) {
		map.entrySet().stream().forEach(this::put);
	}

	/**
	 * Adds the specified entry to this map.
	 * @param entry a map entry
	 */
	default void put(Map.Entry<? extends String, ? extends Object> entry) {
		this.put(entry.getKey(), entry.getValue());
	}

	@Override
	default void clear() {
		this.keySet().stream().forEach(this::remove);
	}

	@Override
	void close();
}
