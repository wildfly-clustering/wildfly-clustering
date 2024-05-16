/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Generic level-one cache implementations.
 * @author Paul Ferraro
 */
public enum CacheStrategy implements CacheFactory {
	/**
	 * Creates a zero-capacity cache, where entries are always "absent".
	 * {@link Cache#computeIfAbsent(Object, BiFunction)} will always creates its value from the specified factory.
	 * Intended for use when some other mechanism already ensures mutually exclusive access to the requested state.
	 */
	NONE() {
		@Override
		public <K, V> Cache<K, V> createCache(Consumer<V> startTask, Consumer<V> stopTask) {
			return new Cache<>() {
				@Override
				public V computeIfAbsent(K key, BiFunction<K, Runnable, V> factory) {
					AtomicReference<V> reference = new AtomicReference<>();
					V value = factory.apply(key, () -> Optional.ofNullable(reference.getPlain()).ifPresent(stopTask::accept));
					if (value != null) {
						startTask.accept(value);
						reference.setPlain(value);
					}
					return value;
				}
			};
		}
	},
	/**
	 * Creates a cache of entries in use by concurrent threads.
	 * Cache entries are auto-removed when the last requestor invokes {@link Runnable#run()} on the {@link Runnable} with which it was created.
	 */
	CONCURRENT() {
		@Override
		public <K, V> Cache<K, V> createCache(Consumer<V> startTask, Consumer<V> stopTask) {
			BiFunction<K, Map.Entry<Integer, AtomicReference<V>>, Map.Entry<Integer, AtomicReference<V>>> addFunction = new BiFunction<>() {
				@Override
				public Map.Entry<Integer, AtomicReference<V>> apply(K id, Map.Entry<Integer, AtomicReference<V>> entry) {
					return (entry != null) ? Map.entry(entry.getKey() + 1, entry.getValue()) : Map.entry(0, new AtomicReference<>());
				}
			};
			BiFunction<K, Map.Entry<Integer, AtomicReference<V>>, Map.Entry<Integer, AtomicReference<V>>> removeFunction = new BiFunction<>() {
				@Override
				public Map.Entry<Integer, AtomicReference<V>> apply(K key, Map.Entry<Integer, AtomicReference<V>> entry) {
					// Entry can be null if entry was already removed, i.e. managed object was already closed
					int count = (entry != null) ? entry.getKey() : 0;
					AtomicReference<V> reference = (entry != null) ? entry.getValue() : null;
					if (count == 0) {
						Optional.ofNullable(reference).map(AtomicReference::getPlain).ifPresent(stopTask::accept);
						// Returning null will remove the map entry
						return null;
					}
					return Map.entry(count - 1, reference);
				}
			};
			return new Cache<>() {
				private final Map<K, Map.Entry<Integer, AtomicReference<V>>> entries = new ConcurrentHashMap<>();

				@Override
				public V computeIfAbsent(K key, BiFunction<K, Runnable, V> factory) {
					Map.Entry<Integer, AtomicReference<V>> entry = this.entries.compute(key, addFunction);
					AtomicReference<V> reference = entry.getValue();
					if (reference.getPlain() == null) {
						synchronized (reference) {
							if (reference.getPlain() == null) {
								Runnable closeTask = () -> this.entries.compute(key, removeFunction);
								V value = factory.apply(key, closeTask);
								if (value != null) {
									startTask.accept(value);
									reference.setPlain(value);
								} else {
									closeTask.run();
								}
							}
						}
					}
					return reference.get();
				}
			};
		}
	},
	;
}
