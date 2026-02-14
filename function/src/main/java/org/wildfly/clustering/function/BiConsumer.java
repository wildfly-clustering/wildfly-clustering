/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 * A consumer with two parameters.
 * @author Paul Ferraro
 * @param <T1> the former parameter type
 * @param <T2> the latter parameter type
 */
public interface BiConsumer<T1, T2> extends java.util.function.BiConsumer<T1, T2>, BinaryObjectOperation<T1, T2>, ToVoidOperation {

	@Override
	default BiConsumer<T1, T2> andThen(java.util.function.BiConsumer<? super T1, ? super T2> after) {
		return of(List.<java.util.function.BiConsumer<? super T1, ? super T2>>of(this, after));
	}

	@Override
	default <V1, V2> BiConsumer<V1, V2> compose(java.util.function.Function<? super V1, ? extends T1> mapper1, java.util.function.Function<? super V2, ? extends T2> mapper2) {
		return new BiConsumer<>() {
			@Override
			public void accept(V1 value1, V2 value2) {
				BiConsumer.this.accept(mapper1.apply(value1), mapper2.apply(value2));
			}
		};
	}

	@Override
	default Consumer<Map.Entry<T1, T2>> composeEntry() {
		return this.composeUnary(Map.Entry::getKey, Map.Entry::getValue);
	}

	@Override
	default <V> Consumer<V> composeUnary(java.util.function.Function<? super V, ? extends T1> mapper1, java.util.function.Function<? super V, ? extends T2> mapper2) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				BiConsumer.this.accept(mapper1.apply(value), mapper2.apply(value));
			}
		};
	}

	@Override
	default BiConsumer<T2, T1> reverse() {
		return new BiConsumer<>() {
			@Override
			public void accept(T2 value1, T1 value2) {
				BiConsumer.this.accept(value2, value1);
			}
		};
	}

	@Override
	default <R> BiFunction<T1, T2, R> thenReturn(java.util.function.Supplier<? extends R> after) {
		return BiFunction.of(this, after);
	}

	@Override
	default BiPredicate<T1, T2> thenReturnBoolean(BooleanSupplier after) {
		return BiPredicate.of(this, after);
	}

	@Override
	default ToDoubleBiFunction<T1, T2> thenReturnDouble(DoubleSupplier after) {
		return ToDoubleBiFunction.of(this, after);
	}

	@Override
	default ToIntBiFunction<T1, T2> thenReturnInt(IntSupplier after) {
		return ToIntBiFunction.of(this, after);
	}

	@Override
	default ToLongBiFunction<T1, T2> thenReturnLong(LongSupplier after) {
		return ToLongBiFunction.of(this, after);
	}

	@Override
	default BiConsumer<T1, T2> thenRun(Runnable after) {
		return BiConsumer.of(this, after);
	}

	/**
	 * Returns a consumer that performs no action.
	 * @param <T> the first consumed type
	 * @param <U> the second consumed type
	 * @return an empty consumer
	 */
	@SuppressWarnings("unchecked")
	static <T, U> BiConsumer<T, U> of() {
		return (BiConsumer<T, U>) EmptyBiConsumer.INSTANCE;
	}

	/**
	 * Returns a composite consumer that delegates to a consumer per parameter.
	 * @param <T1> the first consumed type
	 * @param <T2> the second consumed type
	 * @param former the consumer of the former parameter
	 * @param latter the consumer of the latter parameter
	 * @return a composite consumer
	 */
	static <T1, T2> BiConsumer<T1, T2> of(java.util.function.Consumer<? super T1> former, java.util.function.Consumer<? super T2> latter) {
		return new BiConsumer<>() {
			@Override
			public void accept(T1 value1, T2 value2) {
				former.accept(value1);
				latter.accept(value2);
			}
		};
	}

	/**
	 * Returns a consumer combining the specified operations.
	 * @param <T1> the first consumed type
	 * @param <T2> the second consumed type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <T1, T2> BiConsumer<T1, T2> of(java.util.function.BiConsumer<? super T1, ? super T2> before, Runnable after) {
		return new BiConsumer<>() {
			@Override
			public void accept(T1 value1, T2 value2) {
				before.accept(value1, value2);
				after.run();
			}
		};
	}

	/**
	 * Returns a consumer combining the specified operations.
	 * @param <T1> the first consumed type
	 * @param <T2> the second consumed type
	 * @param <V> the intermediate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <T1, T2, V> BiConsumer<T1, T2> of(java.util.function.BiFunction<? super T1, ? super T2, ? extends V> before, java.util.function.Consumer<? super V> after) {
		return new BiConsumer<>() {
			@Override
			public void accept(T1 value1, T2 value2) {
				after.accept(before.apply(value1, value2));
			}
		};
	}

	/**
	 * Returns a consumer combining the specified operations.
	 * @param <T1> the first consumed type
	 * @param <T2> the second consumed type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <T1, T2> BiConsumer<T1, T2> of(java.util.function.BiPredicate<? super T1, ? super T2> before, BooleanConsumer after) {
		return new BiConsumer<>() {
			@Override
			public void accept(T1 value1, T2 value2) {
				after.accept(before.test(value1, value2));
			}
		};
	}

	/**
	 * Returns a consumer combining the specified operations.
	 * @param <T1> the first consumed type
	 * @param <T2> the second consumed type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <T1, T2> BiConsumer<T1, T2> of(java.util.function.ToDoubleBiFunction<? super T1, ? super T2> before, java.util.function.DoubleConsumer after) {
		return new BiConsumer<>() {
			@Override
			public void accept(T1 value1, T2 value2) {
				after.accept(before.applyAsDouble(value1, value2));
			}
		};
	}

	/**
	 * Returns a consumer combining the specified operations.
	 * @param <T1> the first consumed type
	 * @param <T2> the second consumed type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <T1, T2> BiConsumer<T1, T2> of(java.util.function.ToIntBiFunction<? super T1, ? super T2> before, java.util.function.IntConsumer after) {
		return new BiConsumer<>() {
			@Override
			public void accept(T1 value1, T2 value2) {
				after.accept(before.applyAsInt(value1, value2));
			}
		};
	}

	/**
	 * Returns a consumer combining the specified operations.
	 * @param <T1> the first consumed type
	 * @param <T2> the second consumed type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <T1, T2> BiConsumer<T1, T2> of(java.util.function.ToLongBiFunction<? super T1, ? super T2> before, java.util.function.LongConsumer after) {
		return new BiConsumer<>() {
			@Override
			public void accept(T1 value1, T2 value2) {
				after.accept(before.applyAsLong(value1, value2));
			}
		};
	}

	/**
	 * Returns a composite consumer that delegates to zero or more consumers.
	 * @param <T1> the first consumed type
	 * @param <T2> the second consumed type
	 * @param consumers zero or more consumers
	 * @return a composite consumer
	 */
	static <T1, T2> BiConsumer<T1, T2> of(Iterable<? extends java.util.function.BiConsumer<? super T1, ? super T2>> consumers) {
		return new BiConsumer<>() {
			@Override
			public void accept(T1 value1, T2 value2) {
				for (java.util.function.BiConsumer<? super T1, ? super T2> consumer : consumers) {
					consumer.accept(value1, value2);
				}
			}
		};
	}

	/**
	 * Returns a consumer that delegates to one of two consumers based on the specified predicate.
	 * @param <T1> the former consumer parameter type
	 * @param <T2> the latter consumer parameter type
	 * @param predicate a predicate
	 * @param accepted the consumer to apply when accepted by the specified predicate
	 * @param rejected the consumer to apply when rejected by the specified predicate
	 * @return a consumer that delegates to one of two consumers based on the specified predicate.
	 */
	static <T1, T2> BiConsumer<T1, T2> when(java.util.function.BiPredicate<? super T1, ? super T2> predicate, java.util.function.BiConsumer<? super T1, ? super T2> accepted, java.util.function.BiConsumer<? super T1, ? super T2> rejected) {
		return new BiConsumer<>() {
			@Override
			public void accept(T1 value1, T2 value2) {
				java.util.function.BiConsumer<? super T1, ? super T2> consumer = predicate.test(value1, value2) ? accepted : rejected;
				consumer.accept(value1, value2);
			}
		};
	}

	/**
	 * A consumer that does nothing, ignoring its parameters.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 */
	class EmptyBiConsumer<T1, T2> implements BiConsumer<T1, T2> {
		static final BiConsumer<?, ?> INSTANCE = new EmptyBiConsumer<>();

		private EmptyBiConsumer() {
			// Hide
		}

		@Override
		public void accept(T1 value1, T2 value2) {
			// Do nothing
		}

		@SuppressWarnings("unchecked")
		@Override
		public BiConsumer<T1, T2> andThen(java.util.function.BiConsumer<? super T1, ? super T2> after) {
			return (after instanceof BiConsumer<?, ?> consumer) ? (BiConsumer<T1, T2>) consumer : after::accept;
		}

		@SuppressWarnings("unchecked")
		@Override
		public BiConsumer<T2, T1> reverse() {
			return (BiConsumer<T2, T1>) INSTANCE;
		}
	}
}
