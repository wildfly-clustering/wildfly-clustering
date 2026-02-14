/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.Map;
import java.util.Objects;

/**
 * An enhanced predicate.
 * @author Paul Ferraro
 * @param <V> the test subject type
 */
public interface Predicate<V> extends java.util.function.Predicate<V>, ObjectOperation<V>, ToBooleanOperation {

	@Override
	default Predicate<V> and(java.util.function.Predicate<? super V> other) {
		return Predicate.and(this, other);
	}

	@Override
	default Predicate<V> negate() {
		return Predicate.not(this);
	}

	@Override
	default Predicate<V> or(java.util.function.Predicate<? super V> other) {
		return Predicate.or(this, other);
	}

	@Override
	default <T> Predicate<T> compose(java.util.function.Function<? super T, ? extends V> before) {
		return Predicate.of(before, this);
	}

	@Override
	default <T1, T2> BiPredicate<T1, T2> composeBinary(java.util.function.BiFunction<? super T1, ? super T2, ? extends V> before) {
		return BiPredicate.of(before, this);
	}

	@Override
	default BooleanPredicate composeBoolean(BooleanFunction<? extends V> before) {
		return BooleanPredicate.of(before, this);
	}

	@Override
	default DoublePredicate composeDouble(java.util.function.DoubleFunction<? extends V> before) {
		return DoublePredicate.of(before, this);
	}

	@Override
	default IntPredicate composeInt(java.util.function.IntFunction<? extends V> before) {
		return IntPredicate.of(before, this);
	}

	@Override
	default LongPredicate composeLong(java.util.function.LongFunction<? extends V> before) {
		return LongPredicate.of(before, this);
	}

	@Override
	default Consumer<V> thenAccept(BooleanConsumer consumer) {
		return Consumer.of(this, consumer);
	}

	@Override
	default <R> Function<V, R> thenApply(BooleanFunction<? extends R> after) {
		return Function.of(this, after);
	}

	@Override
	default ToDoubleFunction<V> thenApplyAsDouble(BooleanToDoubleFunction after) {
		return ToDoubleFunction.of(this, after);
	}

	@Override
	default ToIntFunction<V> thenApplyAsInt(BooleanToIntFunction after) {
		return ToIntFunction.of(this, after);
	}

	@Override
	default ToLongFunction<V> thenApplyAsLong(BooleanToLongFunction after) {
		return ToLongFunction.of(this, after);
	}

	@Override
	default Function<V, Boolean> thenBox() {
		return this.thenApply(BooleanPredicate.identity().thenBox());
	}

	@Override
	default Predicate<V> thenTest(BooleanPredicate after) {
		return Predicate.of(this, after);
	}

	/**
	 * Returns a predicate that always evaluates to the specified result.
	 * @param <T> the argument type
	 * @param result the fixed result
	 * @return a predicate that always evaluates to the specified value.
	 */
	@SuppressWarnings("unchecked")
	static <T> Predicate<T> of(boolean result) {
		return (Predicate<T>) (result ? SimplePredicate.ALWAYS : SimplePredicate.NEVER);
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T> the predicate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static <T> Predicate<T> of(java.util.function.Consumer<? super T> before, java.util.function.BooleanSupplier after) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				before.accept(value);
				return after.getAsBoolean();
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T> the predicate type
	 * @param <V> the intermediate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static <T, V> Predicate<T> of(java.util.function.Function<? super T, ? extends V> before, java.util.function.Predicate<? super V> after) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return after.test(before.apply(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T> the predicate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static <T> Predicate<T> of(java.util.function.Predicate<? super T> before, BooleanPredicate after) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return after.test(before.test(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T> the predicate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static <T> Predicate<T> of(java.util.function.ToDoubleFunction<? super T> before, java.util.function.DoublePredicate after) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return after.test(before.applyAsDouble(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T> the predicate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static <T> Predicate<T> of(java.util.function.ToIntFunction<? super T> before, java.util.function.IntPredicate after) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return after.test(before.applyAsInt(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T> the predicate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static <T> Predicate<T> of(java.util.function.ToLongFunction<? super T> before, java.util.function.LongPredicate after) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return after.test(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Composes a predicate that evaluates the conjunction of the specified predicates.
	 * @param <T> the predicate type
	 * @param predicate1 the former operation
	 * @param predicate2 the latter operation
	 * @return a composite predicate
	 */
	static <T> Predicate<T> and(java.util.function.Predicate<? super T> predicate1, java.util.function.Predicate<? super T> predicate2) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return predicate1.test(value) && predicate2.test(value);
			}
		};
	}

	/**
	 * Composes a predicate that evaluates the disjunction of the specified predicates.
	 * @param <T> the predicate type
	 * @param predicate1 the former operation
	 * @param predicate2 the latter operation
	 * @return a composite predicate
	 */
	static <T> Predicate<T> or(java.util.function.Predicate<? super T> predicate1, java.util.function.Predicate<? super T> predicate2) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return predicate1.test(value) || predicate2.test(value);
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param object the object whose reference must match the predicate argument
	 * @param <T> the argument type
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static <T> Predicate<T> equalTo(T object) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return Objects.equals(value, object);
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is identical to the specified object.
	 * @param object the object whose reference must match the predicate argument
	 * @param <T> the argument type
	 * @return a predicate that evaluates to true if and only if the argument is identical to the specified object.
	 */
	static <T> Predicate<T> identicalTo(T object) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return object == value;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is comparatively less than the specified object.
	 * @param object the object whose reference must match the predicate argument
	 * @param <T> the argument type
	 * @return a predicate that evaluates to true if and only if the argument is comparatively less than the specified object.
	 */
	static <T extends Comparable<T>> Predicate<T> lessThan(T object) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return value.compareTo(object) < 0;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is comparatively greater than the specified object.
	 * @param object the object whose reference must match the predicate argument
	 * @param <T> the argument type
	 * @return a predicate that evaluates to true if and only if the argument is comparatively greater than the specified object.
	 */
	static <T extends Comparable<T>> Predicate<T> greaterThan(T object) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return value.compareTo(object) > 0;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to the negation of the specified predicate.
	 * @param predicate the predicate to negate
	 * @param <T> the argument type
	 * @return a predicate that evaluates to the negation of the specified predicate.
	 */
	static <T> Predicate<T> not(java.util.function.Predicate<? super T> predicate) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				return !predicate.test(value);
			}
		};
	}

	/**
	 * Returns a predicate of a {@link java.util.Map.Entry} from the specified key and value predicates.
	 * @param <K> the entry key type
	 * @param <V> the entry value type
	 * @param key an entry key supplier
	 * @param value an entry value supplier
	 * @return a supplier of a {@link java.util.Map.Entry} from the specified key and value suppliers.
	 */
	static <K, V> Predicate<Map.Entry<K, V>> entry(Predicate<K> key, Predicate<V> value) {
		return new Predicate<>() {
			@Override
			public boolean test(Map.Entry<K, V> entry) {
				return key.test(entry.getKey()) && value.test(entry.getValue());
			}
		};
	}

	/**
	 * A predicate that always evaluates to a specified value.
	 * @param <V> the test subject type
	 */
	class SimplePredicate<V> implements Predicate<V> {
		static final Predicate<?> ALWAYS = new SimplePredicate<>(true);
		static final Predicate<?> NEVER = new SimplePredicate<>(false);

		private final boolean result;

		SimplePredicate(boolean result) {
			this.result = result;
		}

		@Override
		public boolean test(V value) {
			return this.result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Predicate<V> and(java.util.function.Predicate<? super V> other) {
			return this.result ? ((other instanceof Predicate<?> predicate) ? (Predicate<V>) predicate : other::test) : (Predicate<V>) NEVER;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Predicate<V> negate() {
			return (Predicate<V>) (this.result ? NEVER : ALWAYS);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Predicate<V> or(java.util.function.Predicate<? super V> other) {
			return this.result ? (Predicate<V>) ALWAYS : ((other instanceof Predicate<?> predicate) ? (Predicate<V>) predicate : other::test);
		}
	}
}
