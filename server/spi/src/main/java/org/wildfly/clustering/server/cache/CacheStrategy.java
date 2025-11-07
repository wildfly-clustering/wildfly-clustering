/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.cache;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.wildfly.clustering.function.Runner;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;

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

				@Override
				public Set<K> keySet() {
					return Set.of();
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
			BiFunction<K, Map.Entry<Integer, StampedLock>, Map.Entry<Integer, StampedLock>> addLockFunction = new PutOrIncrementFunction<>(Map::entry, StampedLock::new);
			BiFunction<K, Map.Entry<Integer, StampedLock>, Map.Entry<Integer, StampedLock>> removeLockFunction = new RemoveOrDecrementFunction<>(Map::entry);
			BiFunction<K, Map.Entry<Integer, AtomicReference<V>>, Map.Entry<Integer, AtomicReference<V>>> addReferenceFunction = new PutOrIncrementFunction<>(AbstractMap.SimpleEntry::new, AtomicReference::new);
			BiFunction<K, Map.Entry<Integer, AtomicReference<V>>, Map.Entry<Integer, AtomicReference<V>>> removeReferenceFunction = new RemoveOrDecrementFunction<>(AbstractMap.SimpleEntry::new);
			return new Cache<>() {
				private final Map<K, Map.Entry<Integer, StampedLock>> locks = new ConcurrentHashMap<>();
				// N.B. We use AtomicReference solely as an object wrapper, updated via getPlain()/setPlain()
				// Thread-safety of plain reference guarded by the corresponding stamped lock
				// StampedLock for a given key needs to be stored in separate map, as its lifecycle may include multiple the reference lifecycles
				private final Map<K, Map.Entry<Integer, AtomicReference<V>>> references = new ConcurrentHashMap<>();

				@Override
				public V computeIfAbsent(K key, BiFunction<K, Runnable, V> factory) {
					V result = null;
					// Create lock (or increment usage) first
					StampedLock lock = this.locks.compute(key, addLockFunction).getValue();
					// Create reference (or increment usage) first
					AtomicReference<V> reference = this.references.compute(key, addReferenceFunction).getValue();
					// Determine if reference is null, and thus we need to invoke factory
					// Try optimistic read first
					long stamp = lock.tryOptimisticRead();
					try {
						// Read optimistically if permitted, must validate later
						result = StampedLock.isOptimisticReadStamp(stamp) ? reference.getPlain() : null;
						if (!lock.validate(stamp)) {
							// Optimistic read unsuccessful or invalid (i.e. another thread holds write lock)
							// Acquire pessimistic read lock
							stamp = lock.readLock();
							// Re-read with read lock
							result = reference.getPlain();
						}
						// If we need to invoke factory, acquire write lock to limit invocation to a single thread
						if (result == null) {
							// Attempt upgrade optimistic/pessimistic read lock to write lock
							long conversionStamp = lock.tryConvertToWriteLock(stamp);
							if (StampedLock.isWriteLockStamp(conversionStamp)) {
								// Lock upgrade successful (no other thread holds any lock)
								stamp = conversionStamp;
							} else {
								// Lock upgrade unsuccessful (another thread holds either a read or write lock)
								// Unlock any previous read lock and wait to acquire write lock
								if (StampedLock.isReadLockStamp(stamp)) {
									lock.unlockRead(stamp);
								}
								stamp = lock.writeLock();
							}
							// Re-read with write lock
							result = reference.getPlain();
						}
						// If still necessary, invoke factory while holding write lock
						// Limits factory invocation to a single thread
						if (result == null) {
							// This task will be run when our reference entry is removed
							Runnable stop = () -> {
								// Invoke stop task while holding write lock
								// Needed to block any future factory invocation until stop task completes
								long stopStamp = lock.writeLock();
								try {
									V value = reference.getPlain();
									if (value != null) {
										stopTask.accept(value);
										reference.setPlain(null);
									}
								} finally {
									lock.unlockWrite(stopStamp);
								}
							};
							// Create object from factory while holding write lock
							result = factory.apply(key, () -> this.remove(key, stop));
							if (result != null) {
								// Invoke start task while holding write lock
								startTask.accept(result);
								reference.setPlain(result);
							}
						}
						return result;
					} finally {
						if (StampedLock.isLockStamp(stamp)) {
							lock.unlock(stamp);
						}
						// If factory returned null, remove map entries (or decrement usage)
						if (result == null) {
							this.remove(key, Runner.empty());
						}
					}
				}

				private void remove(K key, Runnable stopTask) {
					// Invoke stop task if we were the last thread to close the shared object
					if (this.references.compute(key, removeReferenceFunction) == null) {
						stopTask.run();
					}
					// Remove lock (or decrement usage) last
					this.locks.compute(key, removeLockFunction);
				}

				@Override
				public Set<K> keySet() {
					return this.references.keySet();
				}
			};
		}
	},
	;
	private static class AbstractFunction<K, V> implements BiFunction<K, Map.Entry<Integer, V>, Map.Entry<Integer, V>> {
		static final Predicate<Integer> NOT_NULL = Objects::nonNull;
		static final Integer INITIAL_INDEX = Integer.valueOf(0);

		private final BiFunction<Integer, V, Map.Entry<Integer, V>> entryFactory;
		private final UnaryOperator<Integer> indexOperator;
		private final UnaryOperator<V> valueOperator;

		AbstractFunction(BiFunction<Integer, V, Map.Entry<Integer, V>> entryFactory, UnaryOperator<Integer> indexOperator, UnaryOperator<V> valueOperator) {
			this.entryFactory = entryFactory;
			this.indexOperator = indexOperator;
			this.valueOperator = valueOperator;
		}

		@Override
		public Map.Entry<Integer, V> apply(K key, Map.Entry<Integer, V> entry) {
			Integer index = (entry != null) ? entry.getKey() : null;
			V value = (entry != null) ? entry.getValue() : null;
			Integer newIndex = this.indexOperator.apply(index);
			return (newIndex != null) ? this.entryFactory.apply(newIndex, this.valueOperator.apply(value)) : null;
		}
	}

	private static class PutOrIncrementFunction<K, V> extends AbstractFunction<K, V> {
		private static final UnaryOperator<Integer> INCREMENT = Math::incrementExact;
		// If absent, use initial value, otherwise increment.
		private static final UnaryOperator<Integer> PUT_OR_INCREMENT = INCREMENT.orDefault(NOT_NULL, Supplier.of(INITIAL_INDEX));

		PutOrIncrementFunction(BiFunction<Integer, V, Map.Entry<Integer, V>> entryFactory, Supplier<V> factory) {
			super(entryFactory, PUT_OR_INCREMENT, UnaryOperator.<V>identity().orDefault(Objects::nonNull, factory));
		}
	}

	private static class RemoveOrDecrementFunction<K, V> extends AbstractFunction<K, V> {
		private static final UnaryOperator<Integer> DECREMENT = Math::decrementExact;
		// If present and equal to initial value, return null (to remove mapping), otherwise decrement
		private static final UnaryOperator<Integer> REMOVE_OR_DECREMENT = DECREMENT.orDefault(NOT_NULL.and(Predicate.not(INITIAL_INDEX::equals)), Supplier.empty());

		RemoveOrDecrementFunction(BiFunction<Integer, V, Map.Entry<Integer, V>> entryFactory) {
			super(entryFactory, REMOVE_OR_DECREMENT, UnaryOperator.identity());
		}
	}
}
