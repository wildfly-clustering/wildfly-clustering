/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.AbstractMap;
import java.util.Map;

/**
 * An enhanced {@link java.util.function.Function}.
 * @author Paul Ferraro
 * @param <T> the function parameter type
 * @param <R> the function return type
 */
public interface Function<T, R> extends java.util.function.Function<T, R>, ObjectOperation<T>, ToObjectOperation<R> {

	@Override
	default <V> Function<T, V> andThen(java.util.function.Function<? super R, ? extends V> after) {
		return this.thenApply(after);
	}

	@Override
	default <V> Function<V, R> compose(java.util.function.Function<? super V, ? extends T> before) {
		return Function.of(before, this);
	}

	@Override
	default <V1, V2> BiFunction<V1, V2, R> composeBinary(java.util.function.BiFunction<? super V1, ? super V2, ? extends T> before) {
		return BiFunction.of(before, this);
	}

	@Override
	default BooleanFunction<R> composeBoolean(BooleanFunction<? extends T> before) {
		return BooleanFunction.of(before, this);
	}

	@Override
	default DoubleFunction<R> composeDouble(java.util.function.DoubleFunction<? extends T> before) {
		return DoubleFunction.of(before, this);
	}

	@Override
	default IntFunction<R> composeInt(java.util.function.IntFunction<? extends T> before) {
		return IntFunction.of(before, this);
	}

	@Override
	default LongFunction<R> composeLong(java.util.function.LongFunction<? extends T> before) {
		return LongFunction.of(before, this);
	}

	@Override
	default Consumer<T> thenAccept(java.util.function.Consumer<? super R> after) {
		return Consumer.of(this, after);
	}

	@Override
	default <V> Function<T, V> thenApply(java.util.function.Function<? super R, ? extends V> after) {
		return Function.of(this, after);
	}

	@Override
	default ToDoubleFunction<T> thenApplyAsDouble(java.util.function.ToDoubleFunction<? super R> after) {
		return ToDoubleFunction.of(this, after);
	}

	@Override
	default ToIntFunction<T> thenApplyAsInt(java.util.function.ToIntFunction<? super R> after) {
		return ToIntFunction.of(this, after);
	}

	@Override
	default ToLongFunction<T> thenApplyAsLong(java.util.function.ToLongFunction<? super R> after) {
		return ToLongFunction.of(this, after);
	}

	@Override
	default Predicate<T> thenTest(java.util.function.Predicate<? super R> after) {
		return Predicate.of(this, after);
	}

	@Override
	default Function<T, R> thenThrow(java.util.function.Function<? super R, ? extends RuntimeException> exception) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				throw exception.apply(Function.this.apply(value));
			}
		};
	}

	/**
	 * Returns a function that returns its parameter.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @return an identity function
	 */
	@SuppressWarnings("unchecked")
	static <T extends R, R> Function<T, R> identity() {
		return (Function<T, R>) IdentityFunction.INSTANCE;
	}

	/**
	 * Returns a function that always returns the specified value, ignoring its parameter.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @param result the function result
	 * @return a function that always returns the specified value, ignoring its parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T, R> Function<T, R> of(R result) {
		return (result != null) ? new SimpleFunction<>(result) : (Function<T, R>) SimpleFunction.NULL;
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T, R> Function<T, R> of(java.util.function.Consumer<? super T> before, java.util.function.Supplier<? extends R> after) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				before.accept(value);
				return after.get();
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the function parameter type
	 * @param <V> the intermediate type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T, V, R> Function<T, R> of(java.util.function.Function<? super T, ? extends V> before, java.util.function.Function<? super V, ? extends R> after) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				return after.apply(before.apply(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T, R> Function<T, R> of(java.util.function.Predicate<? super T> before, BooleanFunction<? extends R> after) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				return after.apply(before.test(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T, R> Function<T, R> of(java.util.function.ToDoubleFunction<? super T> before, java.util.function.DoubleFunction<? extends R> after) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				return after.apply(before.applyAsDouble(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T, R> Function<T, R> of(java.util.function.ToIntFunction<? super T> before, java.util.function.IntFunction<? extends R> after) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				return after.apply(before.applyAsInt(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T, R> Function<T, R> of(java.util.function.ToLongFunction<? super T> before, java.util.function.LongFunction<? extends R> after) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				return after.apply(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a function that delegates to one of two functions based on the specified predicate.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @param predicate a predicate
	 * @param accepted the function to apply when the specified predicate evaluates to true
	 * @param rejected the function to apply when the specified predicate evaluates to true
	 * @return a function that delegates to one of two functions based on the specified predicate.
	 */
	static <T, R> Function<T, R> when(java.util.function.Predicate<? super T> predicate, java.util.function.Function<? super T, ? extends R> accepted, java.util.function.Function<? super T, ? extends R> rejected) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				java.util.function.Function<? super T, ? extends R> function = predicate.test(value) ? accepted : rejected;
				return function.apply(value);
			}
		};
	}

	/**
	 * Returns a {@link java.util.Map.Entry} function from the specified key and value functions.
	 * @param <K> the entry key type
	 * @param <V> the entry value type
	 * @param <KR> the mapped entry key type
	 * @param <VR> the mapped entry value type
	 * @param keyFunction an entry key function
	 * @param valueFunction an entry value function
	 * @return a {@link java.util.Map.Entry} function from the specified key and value functions.
	 */
	static <K, V, KR, VR> Function<Map.Entry<K, V>, Map.Entry<KR, VR>> entry(Function<? super K, ? extends KR> keyFunction, Function<? super V, ? extends VR> valueFunction) {
		return new Function<>() {
			@Override
			public Map.Entry<KR, VR> apply(Map.Entry<K, V> entry) {
				return new AbstractMap.SimpleImmutableEntry<>(keyFunction.apply(entry.getKey()), valueFunction.apply(entry.getValue()));
			}
		};
	}

	/**
	 * A function that returns a fixed value.
	 * @param <T> the parameter type
	 * @param <R> the return type
	 */
	class SimpleFunction<T, R> implements Function<T, R> {
		static final Function<?, ?> NULL = new SimpleFunction<>(null);

		private R result;

		SimpleFunction(R result) {
			this.result = result;
		}

		@Override
		public R apply(T value) {
			return this.result;
		}
	}

	/**
	 * A function that returns its parameter.
	 * @param <T> the parameter type
	 * @param <R> the return type
	 */
	class IdentityFunction<T extends R, R> implements Function<T, R> {
		static final Function<?, ?> INSTANCE = new IdentityFunction<>();

		IdentityFunction() {
			// Not public
		}

		@Override
		public R apply(T value) {
			return value;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V> Function<V, R> compose(java.util.function.Function<? super V, ? extends T> before) {
			return (before instanceof Function<?, ?> function) ? (Function<V, R>) function : Function.of(before, Function.identity());
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V1, V2> BiFunction<V1, V2, R> composeBinary(java.util.function.BiFunction<? super V1, ? super V2, ? extends T> before) {
			return (before instanceof BiFunction<?, ?, ?> function) ? (BiFunction<V1, V2, R>) function : BiFunction.of(before, Function.identity());
		}

		@SuppressWarnings("unchecked")
		@Override
		public BooleanFunction<R> composeBoolean(BooleanFunction<? extends T> before) {
			return (BooleanFunction<R>) before;
		}

		@SuppressWarnings("unchecked")
		@Override
		public DoubleFunction<R> composeDouble(java.util.function.DoubleFunction<? extends T> before) {
			return (before instanceof DoubleFunction<?> function) ? (DoubleFunction<R>) function : DoubleFunction.of(before, Function.identity());
		}

		@SuppressWarnings("unchecked")
		@Override
		public IntFunction<R> composeInt(java.util.function.IntFunction<? extends T> before) {
			return (before instanceof IntFunction<?> function) ? (IntFunction<R>) function : IntFunction.of(before, Function.identity());
		}

		@SuppressWarnings("unchecked")
		@Override
		public LongFunction<R> composeLong(java.util.function.LongFunction<? extends T> before) {
			return (before instanceof LongFunction<?> function) ? (LongFunction<R>) function : LongFunction.of(before, Function.identity());
		}

		@SuppressWarnings("unchecked")
		@Override
		public Consumer<T> thenAccept(java.util.function.Consumer<? super R> after) {
			return (after instanceof Consumer<?> consumer) ? (Consumer<T>) consumer : Consumer.of(after, Runner.of());
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V> Function<T, V> thenApply(java.util.function.Function<? super R, ? extends V> after) {
			return (after instanceof Function<?, ?> function) ? (Function<T, V>) function : after::apply;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ToDoubleFunction<T> thenApplyAsDouble(java.util.function.ToDoubleFunction<? super R> after) {
			return (after instanceof ToDoubleFunction<?> function) ? (ToDoubleFunction<T>) function : ToDoubleFunction.of(after, DoubleUnaryOperator.identity());
		}

		@SuppressWarnings("unchecked")
		@Override
		public ToIntFunction<T> thenApplyAsInt(java.util.function.ToIntFunction<? super R> after) {
			return (after instanceof ToIntFunction<?> function) ? (ToIntFunction<T>) function : ToIntFunction.of(after, IntUnaryOperator.identity());
		}

		@SuppressWarnings("unchecked")
		@Override
		public ToLongFunction<T> thenApplyAsLong(java.util.function.ToLongFunction<? super R> after) {
			return (after instanceof ToLongFunction<?> function) ? (ToLongFunction<T>) function : ToLongFunction.of(after, LongUnaryOperator.identity());
		}

		@SuppressWarnings("unchecked")
		@Override
		public Predicate<T> thenTest(java.util.function.Predicate<? super R> after) {
			return (after instanceof Predicate<?> predicate) ? (Predicate<T>) predicate : Predicate.of(after, BooleanPredicate.identity());
		}
	}
}
