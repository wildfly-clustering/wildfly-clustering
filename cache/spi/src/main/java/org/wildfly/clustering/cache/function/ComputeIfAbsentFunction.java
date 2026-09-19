/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link java.util.Map#compute(Object, BiFunction)} that implements compute-if-absent semantics.
 * @author Paul Ferraro
 * @param <K> the key type
 * @param <V> the value type
 */
public class ComputeIfAbsentFunction<K, V> implements BiFunction<K, V, V> {
	private final Function<? super K, ? extends V> defaultValueFactory;

	/**
	 * Constructs a new compute-if-absent function.
	 * @param defaultValueFactory the function that compute a default value to return if the existing value is not present.
	 */
	public ComputeIfAbsentFunction(Function<? super K, ? extends V> defaultValueFactory) {
		this.defaultValueFactory = defaultValueFactory;
	}

	@Override
	public V apply(K key, V value) {
		return (value != null) ? value : this.defaultValueFactory.apply(key);
	}

	Function<? super K, ? extends V> getDefaultValueFactory() {
		return this.defaultValueFactory;
	}
}
