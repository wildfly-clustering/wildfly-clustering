/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.AbstractMap;
import java.util.Map;

/**
 * An enhanced supplier.
 * @author Paul Ferraro
 * @param <T> the supplied type
 */
public interface Supplier<T> extends java.util.function.Supplier<T> {
	/** A supplier that always returns null */
	Supplier<?> NULL = () -> null;

	/**
	 * Returns a supplier that returns the value this supplier mapped via the specified function.
	 * @param <V> the mapped value type
	 * @param mapper a mapping function
	 * @return a supplier that returns the value this supplier mapped via the specified function.
	 * @deprecated Superseded by {@link #thenApply(java.util.function.Function)}
	 */
	@Deprecated(forRemoval = true)
	default <V> Supplier<V> map(java.util.function.Function<T, V> mapper) {
		return this.thenApply(mapper);
	}

	/**
	 * Returns a {@link Runnable} that consumes the supplied value.
	 * @param consumer a consumer of the supplied value
	 * @return a {@link Runnable} that consumes the supplied value.
	 */
	default Runnable thenAccept(Consumer<T> consumer) {
		return new Runnable() {
			@Override
			public void run() {
				consumer.accept(Supplier.this.get());
			}
		};
	}

	/**
	 * Returns a supplier that returns the value this supplier mapped via the specified function.
	 * @param <R> the mapped value type
	 * @param function a mapping function
	 * @return a supplier that returns the value this supplier mapped via the specified function.
	 */
	default <R> Supplier<R> thenApply(java.util.function.Function<T, R> function) {
		return new Supplier<>() {
			@Override
			public R get() {
				return function.apply(Supplier.this.get());
			}
		};
	}

	/**
	 * Returns a supplier that returns the value this supplier mapped via the specified function.
	 * @param mapper a mapping function
	 * @return a supplier that returns the value this supplier mapped via the specified function.
	 */
	default DoubleSupplier thenApplyAsDouble(java.util.function.ToDoubleFunction<T> mapper) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				return mapper.applyAsDouble(Supplier.this.get());
			}
		};
	}

	/**
	 * Returns a supplier that returns the value this supplier mapped via the specified function.
	 * @param mapper a mapping function
	 * @return a supplier that returns the value this supplier mapped via the specified function.
	 */
	default IntSupplier thenApplyAsInt(java.util.function.ToIntFunction<T> mapper) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				return mapper.applyAsInt(Supplier.this.get());
			}
		};
	}

	/**
	 * Returns a supplier that returns the value this supplier mapped via the specified function.
	 * @param mapper a mapping function
	 * @return a supplier that returns the value this supplier mapped via the specified function.
	 */
	default LongSupplier thenApplyAsLong(java.util.function.ToLongFunction<T> mapper) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				return mapper.applyAsLong(Supplier.this.get());
			}
		};
	}

	/**
	 * Returns a supplier that returns the value this supplier mapped via the specified predicate.
	 * @param predicate a mapping predicate
	 * @return a supplier that returns the value this supplier mapped via the specified predicate.
	 */
	default BooleanSupplier thenTest(java.util.function.Predicate<T> predicate) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return predicate.test(Supplier.this.get());
			}
		};
	}

	/**
	 * Returns a new supplier that delegates to this supplier using the specified exception handler.
	 * @param handler an exception handler
	 * @return a new supplier that delegates to this supplier using the specified exception handler.
	 */
	default Supplier<T> handle(java.util.function.Function<RuntimeException, T> handler) {
		return new Supplier<>() {
			@Override
			public T get() {
				try {
					return Supplier.this.get();
				} catch (RuntimeException e) {
					return handler.apply(e);
				}
			}
		};
	}

	/**
	 * Returns a supplier that always returns the specified value.
	 * @param <T> the supplied type
	 * @return a supplier that always returns the specified value.
	 */
	@SuppressWarnings("unchecked")
	static <T> Supplier<T> empty() {
		return (Supplier<T>) NULL;
	}

	/**
	 * Returns a supplier that always returns the specified value.
	 * @param <T> the supplied type
	 * @param value the supplied value
	 * @return a supplier that always returns the specified value.
	 */
	static <T> Supplier<T> of(T value) {
		return (value != null) ? new Supplier<>() {
			@Override
			public T get() {
				return value;
			}
		} : empty();
	}

	/**
	 * Returns a supplier that returns null after invoking the specified task.
	 * @param <T> the supplied type
	 * @param task the task to run
	 * @return a supplier that returns null after invoking the specified task.
	 */
	static <T> Supplier<T> run(java.lang.Runnable task) {
		return (task != null) ? new Supplier<>() {
			@Override
			public T get() {
				task.run();
				return null;
			}
		} : empty();
	}

	/**
	 * Returns a supplier that delegates to the specified caller using the specified exception handler.
	 * @param <T> the supplied type
	 * @param caller the caller to call
	 * @param handler an exception handler
	 * @return a supplier that delegates to the specified caller using the specified exception handler.
	 */
	static <T> Supplier<T> call(java.util.concurrent.Callable<T> caller, java.util.function.Function<Exception, T> handler) {
		return (caller != null) ? new Supplier<>() {
			@Override
			public T get() {
				try {
					return caller.call();
				} catch (Exception e) {
					return handler.apply(e);
				}
			}
		} : empty();
	}

	/**
	 * Returns a supplier of a {@link java.util.Map.Entry} from the specified key and value suppliers.
	 * @param <K> the entry key type
	 * @param <V> the entry value type
	 * @param key an entry key supplier
	 * @param value an entry value supplier
	 * @return a supplier of a {@link java.util.Map.Entry} from the specified key and value suppliers.
	 */
	static <K, V> Supplier<Map.Entry<K, V>> entry(Supplier<K> key, Supplier<V> value) {
		return new Supplier<>() {
			@Override
			public Map.Entry<K, V> get() {
				return new AbstractMap.SimpleImmutableEntry<>(key.get(), value.get());
			}
		};
	}
}
