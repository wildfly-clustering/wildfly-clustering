/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.AbstractMap;
import java.util.Map;

/**
 * A supplier of a value.
 * @author Paul Ferraro
 * @param <V> the supplied type
 */
public interface Supplier<V> extends java.util.function.Supplier<V>, VoidOperation, ToObjectOperation<V> {

	@Override
	default Supplier<V> compose(Runnable before) {
		return Supplier.of(before, this);
	}

	@Override
	default <T> Function<T, V> compose(java.util.function.Consumer<? super T> before) {
		return Function.of(before, this);
	}

	@Override
	default <T1, T2> BiFunction<T1, T2, V> composeBinary(java.util.function.BiConsumer<? super T1, ? super T2> before) {
		return BiFunction.of(before, this);
	}

	@Override
	default BooleanFunction<V> composeBoolean(BooleanConsumer before) {
		return BooleanFunction.of(before, this);
	}

	@Override
	default DoubleFunction<V> composeDouble(java.util.function.DoubleConsumer before) {
		return DoubleFunction.of(before, this);
	}

	@Override
	default IntFunction<V> composeInt(java.util.function.IntConsumer before) {
		return IntFunction.of(before, this);
	}

	@Override
	default LongFunction<V> composeLong(java.util.function.LongConsumer before) {
		return LongFunction.of(before, this);
	}

	@Override
	default Runner thenAccept(java.util.function.Consumer<? super V> after) {
		return Runner.of(this, after);
	}

	@Override
	default <R> Supplier<R> thenApply(java.util.function.Function<? super V, ? extends R> after) {
		return of(this, after);
	}

	@Override
	default DoubleSupplier thenApplyAsDouble(java.util.function.ToDoubleFunction<? super V> after) {
		return DoubleSupplier.of(this, after);
	}

	@Override
	default IntSupplier thenApplyAsInt(java.util.function.ToIntFunction<? super V> after) {
		return IntSupplier.of(this, after);
	}

	@Override
	default LongSupplier thenApplyAsLong(java.util.function.ToLongFunction<? super V> after) {
		return LongSupplier.of(this, after);
	}

	@Override
	default BooleanSupplier thenTest(java.util.function.Predicate<? super V> after) {
		return BooleanSupplier.of(this, after);
	}

	@Override
	default Supplier<V> thenThrow(java.util.function.Function<? super V, ? extends RuntimeException> exception) {
		return new Supplier<>() {
			@Override
			public V get() {
				throw exception.apply(Supplier.this.get());
			}
		};
	}

	/**
	 * Returns a supplier that always returns the specified value.
	 * @param <T> the supplied type
	 * @param value the supplied value
	 * @return a supplier that always returns the specified value.
	 */
	@SuppressWarnings("unchecked")
	static <T> Supplier<T> of(T value) {
		return (value != null) ? new SimpleSupplier<>(value) : (Supplier<T>) SimpleSupplier.NULL;
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param <T> the return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static <T> Supplier<T> of(Runnable before, java.util.function.Supplier<? extends T> after) {
		return new Supplier<>() {
			@Override
			public T get() {
				before.run();
				return after.get();
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param <T> the intermediate type
	 * @param <R> the return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static <T, R> Supplier<R> of(java.util.function.Supplier<? extends T> before, java.util.function.Function<? super T, ? extends R> after) {
		return new Supplier<>() {
			@Override
			public R get() {
				return after.apply(before.get());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param <T> the return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static <T> Supplier<T> of(java.util.function.BooleanSupplier before, BooleanFunction<? extends T> after) {
		return new Supplier<>() {
			@Override
			public T get() {
				return after.apply(before.getAsBoolean());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param <T> the return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static <T> Supplier<T> of(java.util.function.DoubleSupplier before, java.util.function.DoubleFunction<? extends T> after) {
		return new Supplier<>() {
			@Override
			public T get() {
				return after.apply(before.getAsDouble());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param <T> the return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static <T> Supplier<T> of(java.util.function.IntSupplier before, java.util.function.IntFunction<? extends T> after) {
		return new Supplier<>() {
			@Override
			public T get() {
				return after.apply(before.getAsInt());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param <T> the return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static <T> Supplier<T> of(java.util.function.LongSupplier before, java.util.function.LongFunction<? extends T> after) {
		return new Supplier<>() {
			@Override
			public T get() {
				return after.apply(before.getAsLong());
			}
		};
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

	/**
	 * A supplier returning a fixed value.
	 * @param <T> the return type
	 */
	class SimpleSupplier<T> implements Supplier<T> {
		static final Supplier<?> NULL = new SimpleSupplier<>(null);

		private final T value;

		SimpleSupplier(T value) {
			this.value = value;
		}

		@Override
		public T get() {
			return this.value;
		}
	}
}
