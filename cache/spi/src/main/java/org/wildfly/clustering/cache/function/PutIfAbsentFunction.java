/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.function.BiFunction;

/**
 * A {@link java.util.Map#compute(Object, BiFunction)} that implements put-if-absent semantics.
 * @author Paul Ferraro
 * @param <K> the key type
 * @param <V> the value type
 */
public class PutIfAbsentFunction<K, V> implements BiFunction<K, V, V> {
	private final V defaultValue;

	/**
	 * Constructs a new put-if-absent function.
	 * @param defaultValue the value to return if the existing value is not present.
	 */
	public PutIfAbsentFunction(V defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public V apply(K key, V value) {
		return (value != null) ? value : this.defaultValue;
	}

	V getDefaultValue() {
		return this.defaultValue;
	}
}
