/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Encapsulates thread-safe references to map entries.
 * @author Paul Ferraro
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ReferenceMap<K, V> extends ReadableReference<Map<K, V>> {
	/**
	 * Returns a reference to the map entry for the specified key.
	 * @param key a map key
	 * @return a reference to the map entry for the specified key.
	 */
	Reference<V> reference(K key);

	/**
	 * Returns a thread-safe map of the specified map.
	 * @param <K> the map key type
	 * @param <V> the map value type
	 * @param map a non-thread-safe map
	 * @return a thread-safe map of the specified map.
	 */
	static <K, V> ReferenceMap<K, V> of(Map<K, V> map) {
		StampedLock lock = new StampedLock();
		Supplier<Map<K, V>> reader = () -> Collections.unmodifiableMap(map);
		UnaryOperator<Map<K, V>> mapper = UnaryOperator.identity();
		return new ReferenceMap<>() {
			@Override
			public Reference<V> reference(K key) {
				Supplier<V> reader = () -> map.get(key);
				Consumer<V> writer = value -> map.put(key, value);
				UnaryOperator<V> mapper = UnaryOperator.identity();
				return new Reference<>() {
					@Override
					public Reader<V> reader() {
						return new ReferenceReader<>(lock, reader, mapper);
					}

					@Override
					public Writer<V> writer(UnaryOperator<V> updater) {
						return new ReferenceWriter<>(lock, reader, writer, mapper, updater);
					}
				};
			}

			@Override
			public Reader<Map<K, V>> reader() {
				return new ReferenceReader<>(lock, reader, mapper);
			}
		};
	}
}
