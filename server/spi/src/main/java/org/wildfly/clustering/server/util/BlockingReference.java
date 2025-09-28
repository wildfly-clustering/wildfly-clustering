/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;

/**
 * Encapsulates thread-safe reading/writing on an object reference.
 * Analogous to {@link AtomicReference}, but uses read/write locks instead of CAS operations.
 * @author Paul Ferraro
 * @param <T> the referenced type
 */
public interface BlockingReference<T> extends Reference<T> {

	/**
	 * Returns a thread-safe writer of this reference.
	 * @param value the target value of this reference
	 * @return a thread-safe reader of this reference.
	 */
	default Writer<T> writer(T value) {
		return this.writer(Supplier.of(value));
	}

	/**
	 * Returns a thread-safe writer of this reference.
	 * @param supplier supplier of the target value of this reference
	 * @return a thread-safe reader of this reference.
	 */
	default Writer<T> writer(java.util.function.Supplier<T> supplier) {
		return this.writer(UnaryOperator.get(supplier));
	}

	/**
	 * Returns a thread-safe writer of this reference.
	 * @param updater operator returning the target value of this reference based on the current value
	 * @return a thread-safe reader of this reference.
	 */
	Writer<T> writer(java.util.function.UnaryOperator<T> updater);

	/**
	 * Describes the writer of a reference.
	 * @param <T> the referenced type
	 */
	interface Writer<T> extends java.util.function.Supplier<T> {
		/**
		 * Returns a mapped writer, whose mapping function is invoked while holding a lock.
		 * @param <R> the type of the mapped writer
		 * @param mapper a mapping function
		 * @return a mapped writer
		 */
		<R> Writer<R> map(Function<T, R> mapper);

		/**
		 * Returns a supplier whose {@link Supplier#get()} will update the reference only when the specified condition (invoked while holding a lock) is met.
		 * @param condition a condition for which this reference should be updated
		 * @return a supplier for updating this reference.
		 */
		java.util.function.Supplier<T> when(Predicate<T> condition);
	}

	/**
	 * Returns a blocking reference with the specified initial value.
	 * @param <T> the referenced object type
	 * @param initialValue the initial value of the returned reference
	 * @return a blocking reference with the specified initial value.
	 */
	static <T> BlockingReference<T> of(T initialValue) {
		AtomicReference<T> reference = new AtomicReference<>(initialValue);
		StampedLock lock = new StampedLock();
		Supplier<T> reader = reference::getPlain;
		Consumer<T> writer = reference::setPlain;
		return new BlockingReference<>() {
			@Override
			public Reader<T> reader() {
				return new ReferenceReader<>(lock, reader, Function.identity());
			}

			@Override
			public Writer<T> writer(java.util.function.UnaryOperator<T> updater) {
				return new ReferenceWriter<>(lock, reader, writer, Function.identity(), updater);
			}
		};
	}

	/**
	 * A writer implementation for a reference.
	 * @param <T> the referenced object type
	 * @param <V> the mapped type
	 */
	class ReferenceWriter<T, V> implements Writer<V> {
		private final StampedLock lock;
		private final java.util.function.Supplier<T> reader;
		private final Consumer<T> writer;
		private final Function<T, V> mapper;
		private final java.util.function.UnaryOperator<T> updater;

		ReferenceWriter(StampedLock lock, java.util.function.Supplier<T> reader, Consumer<T> writer, Function<T, V> mapper, java.util.function.UnaryOperator<T> updater) {
			this.lock = lock;
			this.reader = reader;
			this.writer = writer;
			this.mapper = mapper;
			this.updater = updater;
		}

		@Override
		public V get() {
			long stamp = this.lock.writeLock();
			try {
				T value = this.reader.get();
				this.writer.accept(this.updater.apply(value));
				return this.mapper.apply(value);
			} finally {
				this.lock.unlockWrite(stamp);
			}
		}

		@Override
		public <R> Writer<R> map(Function<V, R> mapper) {
			return new ReferenceWriter<>(this.lock, this.reader, this.writer, this.mapper.andThen(mapper), this.updater);
		}

		@Override
		public java.util.function.Supplier<V> when(Predicate<V> condition) {
			return new ConditionalReferenceWriter<>(this.lock, this.reader, this.writer, this.mapper, condition, this.updater);
		}
	}

	/**
	 * A conditional writer implementation for a reference.
	 * @param <T> the referenced object type
	 * @param <V> the mapped type
	 */
	class ConditionalReferenceWriter<T, V> implements java.util.function.Supplier<V> {
		private final StampedLock lock;
		private final java.util.function.Supplier<T> reader;
		private final Consumer<T> writer;
		private final Function<T, V> mapper;
		private final Predicate<V> condition;
		private final java.util.function.UnaryOperator<T> updater;

		ConditionalReferenceWriter(StampedLock lock, java.util.function.Supplier<T> reader, Consumer<T> writer, Function<T, V> mapper, Predicate<V> condition, java.util.function.UnaryOperator<T> updater) {
			this.lock = lock;
			this.reader = reader;
			this.writer = writer;
			this.mapper = mapper;
			this.condition = condition;
			this.updater = updater;
		}

		@Override
		public V get() {
			T value = null;
			V result = null;
			boolean update = false;
			// Try optimistic read first
			long stamp = this.lock.tryOptimisticRead();
			try {
				if (StampedLock.isOptimisticReadStamp(stamp)) {
					// Read optimistically, and validate later
					value = this.reader.get();
					result = this.mapper.apply(value);
					update = this.condition.test(result);
				}
				if (!this.lock.validate(stamp)) {
					// Optimistic read unsuccessful or invalid
					// Acquire pessimistic read lock
					stamp = this.lock.readLock();
					// Re-read with read lock
					value = this.reader.get();
					result = this.mapper.apply(value);
					update = this.condition.test(result);
				}
				if (update) {
					long conversionStamp = this.lock.tryConvertToWriteLock(stamp);
					if (StampedLock.isWriteLockStamp(conversionStamp)) {
						// Conversion successful
						stamp = conversionStamp;
					} else {
						// Conversion unsuccessful, release any pessimistic read lock and acquire write lock
						if (StampedLock.isReadLockStamp(stamp)) {
							this.lock.unlockRead(stamp);
						}
						stamp = this.lock.writeLock();
					}
					// Re-read with write lock
					value = this.reader.get();
					result = this.mapper.apply(value);
					if (this.condition.test(result)) {
						this.writer.accept(this.updater.apply(value));
					}
				}
				return result;
			} finally {
				if (StampedLock.isLockStamp(stamp)) {
					this.lock.unlock(stamp);
				}
			}
		}
	}
}
