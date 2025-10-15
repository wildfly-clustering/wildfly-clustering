/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.Map;
import java.util.Objects;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

/**
 * An enhanced predicate.
 * @author Paul Ferraro
 * @param <T> the argument type
 */
public interface Predicate<T> extends java.util.function.Predicate<T> {
	/** A predicate that always returns true */
	Predicate<?> ALWAYS = of(Consumer.EMPTY, BooleanSupplier.TRUE);
	/** A predicate that always returns false */
	Predicate<?> NEVER = of(Consumer.EMPTY, BooleanSupplier.FALSE);

	/**
	 * Returns a new predicate that delegates to this predicate using the specified exception handler.
	 * @param handler an exception handler
	 * @return a new predicate that delegates to this predicate using the specified exception handler.
	 */
	default Predicate<T> handle(java.util.function.BiPredicate<T, RuntimeException> handler) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				try {
					return Predicate.this.test(value);
				} catch (RuntimeException e) {
					return handler.test(value, e);
				}
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping function
	 * @param <V> the return type of the composed predicate
	 * @return a composed predicate
	 */
	default <V> Predicate<V> compose(Function<V, T> function) {
		return new Predicate<>() {
			@Override
			public boolean test(V test) {
				return Predicate.this.test(function.apply(test));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param function a mapping function
	 * @return a composed predicate
	 */
	default <V1, V2> BiPredicate<V1, V2> composeBinary(BiFunction<V1, V2, T> function) {
		return new BiPredicate<>() {
			@Override
			public boolean test(V1 test1, V2 test2) {
				return Predicate.this.test(function.apply(test1, test2));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping function
	 * @return a composed predicate
	 */
	default DoublePredicate composeDouble(DoubleFunction<T> function) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return Predicate.this.test(function.apply(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping function
	 * @return a composed predicate
	 */
	default IntPredicate composeInt(IntFunction<T> function) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return Predicate.this.test(function.apply(value));
			}
		};
	}

	/**
	 * Returns a predicate that applies the specified function to its argument before evaluating.
	 * @param function a mapping function
	 * @return a composed predicate
	 */
	default LongPredicate composeLong(LongFunction<T> function) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return Predicate.this.test(function.apply(value));
			}
		};
	}

	@Override
	default Predicate<T> negate() {
		return new Predicate<>() {
			@Override
			public boolean test(T test) {
				return !Predicate.this.test(test);
			}
		};
	}

	@Override
	default Predicate<T> and(java.util.function.Predicate<? super T> other) {
		return new Predicate<>() {
			@Override
			public boolean test(T test) {
				return Predicate.this.test(test) && other.test(test);
			}
		};
	}

	@Override
	default Predicate<T> or(java.util.function.Predicate<? super T> other) {
		return new Predicate<>() {
			@Override
			public boolean test(T test) {
				return Predicate.this.test(test) || other.test(test);
			}
		};
	}

	/**
	 * Returns a predicate returning the exclusive disjunction of this predicate with the specified predicate.
	 * @param other another predicate
	 * @return a predicate returning the exclusive disjunction of this predicate with the specified predicate.
	 */
	default Predicate<T> xor(java.util.function.Predicate<? super T> other) {
		return new Predicate<>() {
			@Override
			public boolean test(T test) {
				return Predicate.this.test(test) ^ other.test(test);
			}
		};
	}

	/**
	 * Returns a predicate that always evaluates to the specified result.
	 * @param <T> the argument type
	 * @param result the fixed result
	 * @return a predicate that always evaluates to the specified value.
	 */
	static <T> Predicate<T> of(boolean result) {
		return result ? Predicate.always() : Predicate.never();
	}

	/**
	 * Returns a predicate that accepts its parameter via the specified consumer and returns the value returned by the specified supplier.
	 * @param <T> the predicate type
	 * @param consumer the consumer of the predicate parameter
	 * @param supplier the supplier of the predicate result
	 * @return a predicate that accepts its parameter via the specified consumer and returns the value returned by the specified supplier.
	 */
	static <T> Predicate<T> of(java.util.function.Consumer<T> consumer, java.util.function.BooleanSupplier supplier) {
		return new Predicate<>() {
			@Override
			public boolean test(T value) {
				consumer.accept(value);
				return supplier.getAsBoolean();
			}
		};
	}

	/**
	 * Returns a predicate that always accepts its argument.
	 * @param <T> the argument type
	 * @return a predicate that always accepts its argument.
	 */
	@SuppressWarnings("unchecked")
	static <T> Predicate<T> always() {
		return (Predicate<T>) ALWAYS;
	}

	/**
	 * Returns a predicate that never accepts its argument.
	 * @param <T> the argument type
	 * @return a predicate that never accepts its argument.
	 */
	@SuppressWarnings("unchecked")
	static <T> Predicate<T> never() {
		return (Predicate<T>) NEVER;
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
	@SuppressWarnings("unchecked")
	static <T> Predicate<T> not(Predicate<? super T> predicate) {
		return (Predicate<T>) predicate.negate();
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
}
