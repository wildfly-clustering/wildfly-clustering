/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.context;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Implementations for creating contexts.
 * @author Paul Ferraro
 */
public enum ContextStrategy implements ContextFactory {
	/**
	 * Creates an unshared context, where state is always "absent".
	 * {@link Context#computeIfAbsent(Object, BiFunction)} will always creates its value from the specified factory.
	 * Intended for use when some other mechanism already ensures mutually exclusive access to the requested state.
	 */
	UNSHARED() {
		@Override
		public <K, V> Context<K, V> createContext(Consumer<V> startTask, Consumer<V> stopTask) {
			return new Context<>() {
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
	 * Creates a context storing sharable references.
	 * Concurrent {@link Context#computeIfAbsent(Object, BiFunction)} invocations for the same key will reference the same state, until all references are closed.
	 */
	SHARED() {
		@Override
		public <K, V> Context<K, V> createContext(Consumer<V> startTask, Consumer<V> stopTask) {
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
			return new Context<>() {
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
