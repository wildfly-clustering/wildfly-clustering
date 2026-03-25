/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;

/**
 * Encapsulates thread-safe reading/writing on an object reference.
 * Analogous to {@link AtomicReference}, but uses read/write locks instead of CAS operations.
 * @author Paul Ferraro
 * @param <T> the reference type
 */
public interface BlockingReference<T> extends Reference<T> {

	/**
	 * Returns a thread-safe writer of this reference.
	 * @return a thread-safe writer of this reference.
	 */
	Writer<T> getWriter();

	/**
	 * Returns a conditional thread-safe writer of this reference.
	 * The returned writer will only perform updates when the specified predicate is met, avoid the need for unnecessary write lock acquisition.
	 * @param when a predicate that must be met for a given write operation to proceed.
	 * @return a conditional thread-safe writer of this reference.
	 */
	Writer<T> getWriter(Predicate<? super T> when);

	/**
	 * Describes the writer of a reference.
	 * @param <T> the reference type
	 */
	interface Writer<T> {

		/**
		 * Updates the value of this reference using the result of the specified supplier.
		 * @param writer a supplier of the new value
		 * @return the old value
		 */
		default T getAndSet(java.util.function.Supplier<T> writer) {
			return this.set(writer).getKey();
		}

		/**
		 * Updates the value of this reference based on its current value.
		 * @param writer a function that returns the new value given its current value.
		 * @return the old value
		 */
		default T getAndUpdate(java.util.function.UnaryOperator<T> writer) {
			return this.update(writer).getKey();
		}

		/**
		 * Updates the value of this reference using the result of the specified supplier.
		 * @param writer a supplier of the new value
		 * @return the new value
		 */
		default T setAndGet(java.util.function.Supplier<T> writer) {
			return this.set(writer).getValue();
		}

		/**
		 * Updates the value of this reference based on its current value.
		 * @param writer a function that returns the new value given its current value.
		 * @return the new value
		 */
		default T updateAndGet(java.util.function.UnaryOperator<T> writer) {
			return this.update(writer).getValue();
		}

		/**
		 * Updates the value of this reference using the result of the specified supplier.
		 * @param writer a supplier of the new value
		 * @return a map entry containing the previous and current values of this reference
		 */
		default Map.Entry<T, T> set(java.util.function.Supplier<T> writer) {
			return this.update(UnaryOperator.of(Consumer.of(), writer));
		}

		/**
		 * Returns a map entry containing the previous and current value after applying the specified update function.
		 * @param writer a function that returns the new value given its current value.
		 * @return a map entry containing the previous and current values of this reference
		 */
		Map.Entry<T, T> update(java.util.function.UnaryOperator<T> writer);
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
		Reader<T> blockingReader = new BlockingReferenceReader<>(lock, reader, Function.identity());
		Writer<T> blockingWriter = new BlockingReferenceWriter<>(lock, reader, writer);
		return new BlockingReference<>() {
			@Override
			public Reader<T> getReader() {
				return blockingReader;
			}

			@Override
			public Writer<T> getWriter() {
				return blockingWriter;
			}

			@Override
			public Writer<T> getWriter(Predicate<? super T> when) {
				return new ConditionalReferenceWriter<>(lock, reader, writer, when);
			}
		};
	}

	/**
	 * A reader of a blocking reference.
	 * {@link #read(java.util.function.Consumer)} consumes the referenced value while holding a pessimistic read lock.
	 * {@link #get()} reads the reference using an optimistic read lock, when possible.
	 * @param <T> the reference type
	 * @param <V> the reader type
	 */
	class BlockingReferenceReader<T, V> implements Reader<V> {
		private final StampedLock lock;
		private final Supplier<T> reader;
		private final java.util.function.Function<? super T, ? extends V> mapper;

		BlockingReferenceReader(StampedLock lock, Supplier<T> reader, java.util.function.Function<? super T, ? extends V> mapper) {
			this.lock = lock;
			this.reader = reader;
			this.mapper = mapper;
		}

		@Override
		public void read(java.util.function.Consumer<? super V> consumer) {
			long stamp = this.lock.readLock();
			try {
				T value = this.reader.get();
				V mapped = this.mapper.apply(value);
				consumer.accept(mapped);
			} finally {
				this.lock.unlockRead(stamp);
			}
		}

		@Override
		public V get() {
			V result = null;
			// Try optimistic read first
			long stamp = this.lock.tryOptimisticRead();
			try {
				if (StampedLock.isOptimisticReadStamp(stamp)) {
					// Read optimistically, but validate later
					T value = this.reader.get();
					result = this.mapper.apply(value);
				}
				if (!this.lock.validate(stamp)) {
					// Optimistic read invalid
					// Acquire pessimistic read lock
					stamp = this.lock.readLock();
					// Re-read with read lock
					T value = this.reader.get();
					result = this.mapper.apply(value);
				}
				return result;
			} finally {
				if (StampedLock.isReadLockStamp(stamp)) {
					this.lock.unlockRead(stamp);
				}
			}
		}

		@Override
		public <R> Reader<R> map(java.util.function.Function<? super V, ? extends R> mapper) {
			return new BlockingReferenceReader<>(this.lock, this.reader, this.mapper.andThen(mapper));
		}
	}

	/**
	 * A writer of a blocking reference.
	 * All write operations require write locks.
	 * @param <T> the reference type
	 */
	class BlockingReferenceWriter<T> implements Writer<T> {
		private final StampedLock lock;
		private final Supplier<T> reader;
		private final Consumer<T> writer;

		BlockingReferenceWriter(StampedLock lock, Supplier<T> reader, Consumer<T> writer) {
			this.lock = lock;
			this.reader = reader;
			this.writer = writer;
		}

		@Override
		public Map.Entry<T, T> update(java.util.function.UnaryOperator<T> writer) {
			long stamp = this.lock.writeLock();
			try {
				T value = this.reader.get();
				T updated = writer.apply(value);
				this.writer.accept(updated);
				return new AbstractMap.SimpleImmutableEntry<>(value, updated);
			} finally {
				this.lock.unlockWrite(stamp);
			}
		}
	}

	/**
	 * A conditional writer of a blocking reference.
	 * Attempts to evaluate condition using an optimistic read, when possible.
	 * @param <T> the reference type
	 */
	class ConditionalReferenceWriter<T> implements Writer<T> {
		private final StampedLock lock;
		private final Supplier<T> reader;
		private final Consumer<T> writer;
		private final Predicate<? super T> condition;

		ConditionalReferenceWriter(StampedLock lock, Supplier<T> reader, Consumer<T> writer, Predicate<? super T> condition) {
			this.lock = lock;
			this.reader = reader;
			this.writer = writer;
			this.condition = condition;
		}

		@Override
		public Map.Entry<T, T> update(java.util.function.UnaryOperator<T> writer) {
			T value = null;
			boolean update = false;
			// Try optimistic read first
			long stamp = this.lock.tryOptimisticRead();
			try {
				if (StampedLock.isOptimisticReadStamp(stamp)) {
					// Read optimistically, and validate later
					value = this.reader.get();
					update = this.condition.test(value);
				}
				if (!this.lock.validate(stamp)) {
					// Optimistic read unsuccessful or invalid
					// Acquire pessimistic read lock
					stamp = this.lock.readLock();
					// Re-read with read lock
					value = this.reader.get();
					update = this.condition.test(value);
				}
				T updated = value;
				if (update) {
					// Attempt lock conversion
					long conversionStamp = this.lock.tryConvertToWriteLock(stamp);
					if (StampedLock.isWriteLockStamp(conversionStamp)) {
						// Conversion successful, no need to re-read
						stamp = conversionStamp;
					} else {
						// Conversion unsuccessful, release any pessimistic read lock and acquire write lock
						if (StampedLock.isReadLockStamp(stamp)) {
							this.lock.unlockRead(stamp);
						}
						stamp = this.lock.writeLock();
						// Re-read with write lock
						value = this.reader.get();
						update = this.condition.test(value);
					}
					if (update) {
						// Compute new value while holding write lock
						updated = writer.apply(value);
						this.writer.accept(updated);
					}
				}
				return new AbstractMap.SimpleImmutableEntry<>(value, updated);
			} finally {
				if (StampedLock.isLockStamp(stamp)) {
					this.lock.unlock(stamp);
				}
			}
		}
	}
}
