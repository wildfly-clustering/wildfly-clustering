/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A readable reference.
 * @author Paul Ferraro
 * @param <T> the type of this reference
 */
public interface ReadableReference<T> {

	/**
	 * Returns a thread-safe reader of this reference.
	 * @return a thread-safe reader of this reference.
	 */
	Reader<T> reader();

	interface Reader<T> extends Supplier<T> {
		/**
		 * Consumes the referenced value while holding a pessimistic read lock.
		 * @param consumer a consumer of the referenced value
		 */
		void consume(Consumer<T> consumer);

		/**
		 * Maps this referenced value using the specified mapping function while holding a read lock.
		 * @param <R> the mapped type
		 * @param mapper a mapping function
		 * @return a reader of the mapped reference.
		 */
		<R> Reader<R> map(Function<T, R> mapper);
	}

	class ReferenceReader<T, V> implements Reader<V> {
		private final StampedLock lock;
		private final Supplier<T> reader;
		private final Function<T, V> mapper;

		ReferenceReader(StampedLock lock,Supplier<T> reader, Function<T, V> mapper) {
			this.lock = lock;
			this.reader = reader;
			this.mapper = mapper;
		}

		@Override
		public <R> Reader<R> map(Function<V, R> mapper) {
			return new ReferenceReader<>(this.lock, this.reader, this.mapper.andThen(mapper));
		}

		@Override
		public void consume(Consumer<V> consumer) {
			long stamp = this.lock.readLock();
			try {
				T value = this.reader.get();
				V result = this.mapper.apply(value);
				consumer.accept(result);
			} finally {
				this.lock.unlockRead(stamp);
			}
		}

		@Override
		public V get() {
			T value = null;
			V result = null;
			// Try optimistic read first
			long stamp = this.lock.tryOptimisticRead();
			try {
				if (StampedLock.isOptimisticReadStamp(stamp)) {
					// Read optimistically, but validate later
					value = this.reader.get();
					result = this.mapper.apply(value);
				}
				if (!this.lock.validate(stamp)) {
					// Optimistic read invalid
					// Acquire pessimistic read lock
					stamp = this.lock.readLock();
					// Re-read with read lock
					value = this.reader.get();
					result = this.mapper.apply(value);
				}
				return result;
			} finally {
				if (StampedLock.isReadLockStamp(stamp)) {
					this.lock.unlockRead(stamp);
				}
			}
		}
	}
}
