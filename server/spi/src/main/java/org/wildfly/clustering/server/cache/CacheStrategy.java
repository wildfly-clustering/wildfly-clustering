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
import java.util.function.Supplier;

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
			BiFunction<K, Map.Entry<Integer, Object>, Map.Entry<Integer, Object>> addMutexFunction = new AddIfAbsentFunction<>(Object::new);
			BiFunction<K, Map.Entry<Integer, Object>, Map.Entry<Integer, Object>> removeMutexFunction = new RemoveIfPresentFunction<>();
			BiFunction<K, Map.Entry<Integer, AtomicReference<V>>, Map.Entry<Integer, AtomicReference<V>>> addReferenceFunction = new AddIfAbsentFunction<>(AtomicReference::new);
			BiFunction<K, Map.Entry<Integer, AtomicReference<V>>, Map.Entry<Integer, AtomicReference<V>>> removeReferenceFunction = new RemoveIfPresentFunction<>();
			return new Cache<>() {
				private final Map<K, Map.Entry<Integer, Object>> mutexes = new ConcurrentHashMap<>();
				private final Map<K, Map.Entry<Integer, AtomicReference<V>>> references = new ConcurrentHashMap<>();

				@Override
				public V computeIfAbsent(K key, BiFunction<K, Runnable, V> factory) {
					Object mutex = this.mutexes.compute(key, addMutexFunction).getValue();
					AtomicReference<V> reference = this.references.compute(key, addReferenceFunction).getValue();
					V result = reference.get();
					if (result == null) {
						// Restrict creation to a single thread
						synchronized (mutex) {
							result = reference.get();
							if (result == null) {
								Runnable closeTask = () -> {
									// Block attempts to recreate object until stop task completes
									if (this.references.compute(key, removeReferenceFunction) == null) {
										synchronized (mutex) {
											V value = reference.get();
											if (value != null) {
												stopTask.accept(value);
											}
										}
									}
									this.mutexes.compute(key, removeMutexFunction);
								};
								result = factory.apply(key, closeTask);
								if (result != null) {
									startTask.accept(result);
									reference.set(result);
								} else {
									closeTask.run();
								}
							}
						}
					}
					return result;
				}
			};
		}
	},
	;
	private static class AddIfAbsentFunction<K, V> implements BiFunction<K, Map.Entry<Integer, V>, Map.Entry<Integer, V>> {
		private Supplier<V> factory;

		AddIfAbsentFunction(Supplier<V> factory) {
			this.factory = factory;
		}

		@Override
		public Map.Entry<Integer, V> apply(K id, Map.Entry<Integer, V> entry) {
			return (entry != null) ? Map.entry(entry.getKey() + 1, entry.getValue()) : Map.entry(0, this.factory.get());
		}
	}

	private static class RemoveIfPresentFunction<K, V> implements BiFunction<K, Map.Entry<Integer, V>, Map.Entry<Integer, V>> {
		@Override
		public Map.Entry<Integer, V> apply(K id, Map.Entry<Integer, V> entry) {
			int count = (entry != null) ? entry.getKey() : 0;
			// Returning null will remove the map entry
			return (count > 0) ? Map.entry(count - 1, entry.getValue()) : null;
		}
	}
}
