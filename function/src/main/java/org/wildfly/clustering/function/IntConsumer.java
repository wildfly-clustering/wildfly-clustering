/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;

/**
 * An enhanced integer consumer.
 * @author Paul Ferraro
 */
public interface IntConsumer extends java.util.function.IntConsumer, IntOperation, PrimitiveConsumer<Integer> {

	@Override
	default IntConsumer andThen(java.util.function.IntConsumer after) {
		return of(List.of(this, after));
	}

	@Override
	default Consumer<Integer> box() {
		return this.compose(IntUnaryOperator.identity().box());
	}

	@Override
	default <V> Consumer<V> compose(java.util.function.ToIntFunction<? super V> before) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				IntConsumer.this.accept(before.applyAsInt(value));
			}
		};
	}

	@Override
	default <V1, V2> BiConsumer<V1, V2> composeBinary(java.util.function.ToIntBiFunction<? super V1, ? super V2> composer) {
		return new BiConsumer<>() {
			@Override
			public void accept(V1 value1, V2 value2) {
				IntConsumer.this.accept(composer.applyAsInt(value1, value2));
			}
		};
	}

	@Override
	default BooleanConsumer composeBoolean(BooleanToIntFunction before) {
		return BooleanConsumer.of(before, this);
	}

	@Override
	default DoubleConsumer composeDouble(java.util.function.DoubleToIntFunction before) {
		return DoubleConsumer.of(before, this);
	}

	@Override
	default IntConsumer composeInt(java.util.function.IntUnaryOperator before) {
		return IntConsumer.of(before, this);
	}

	@Override
	default LongConsumer composeLong(java.util.function.LongToIntFunction before) {
		return LongConsumer.of(before, this);
	}

	@Override
	default <R> IntFunction<R> thenReturn(java.util.function.Supplier<? extends R> factory) {
		return IntFunction.of(this, factory);
	}

	@Override
	default IntPredicate thenReturnBoolean(java.util.function.BooleanSupplier after) {
		return IntPredicate.of(this, after);
	}

	@Override
	default IntToDoubleFunction thenReturnDouble(java.util.function.DoubleSupplier after) {
		return IntToDoubleFunction.of(this, after);
	}

	@Override
	default IntUnaryOperator thenReturnInt(java.util.function.IntSupplier after) {
		return IntUnaryOperator.of(this, after);
	}

	@Override
	default IntToLongFunction thenReturnLong(java.util.function.LongSupplier after) {
		return IntToLongFunction.of(this, after);
	}

	@Override
	default IntConsumer thenRun(Runnable after) {
		return of(this, after);
	}

	/**
	 * Returns a consumer that ignores its parameter.
	 * @return a consumer that ignores its parameter.
	 */
	static IntConsumer of() {
		return EmptyIntConsumer.INSTANCE;
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static IntConsumer of(java.util.function.IntConsumer before, Runnable after) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				before.accept(value);
				after.run();
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static IntConsumer of(java.util.function.IntPredicate before, BooleanConsumer after) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				after.accept(before.test(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T> the intermediate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static <T> IntConsumer of(java.util.function.IntFunction<? extends T> before, java.util.function.Consumer<? super T> after) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				after.accept(before.apply(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static IntConsumer of(java.util.function.IntToDoubleFunction before, java.util.function.DoubleConsumer after) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				after.accept(before.applyAsDouble(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static IntConsumer of(java.util.function.IntUnaryOperator before, java.util.function.IntConsumer after) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				after.accept(before.applyAsInt(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static IntConsumer of(java.util.function.IntToLongFunction before, java.util.function.LongConsumer after) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				after.accept(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a composite consumer that delegates to the specified consumers.
	 * @param consumers a number of consumers
	 * @return a composite consumer
	 */
	static IntConsumer of(Iterable<? extends java.util.function.IntConsumer> consumers) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				for (java.util.function.IntConsumer consumer : consumers) {
					consumer.accept(value);
				}
			}
		};
	}

	/**
	 * Returns a consumer that delegates to one of two consumers based on the specified predicate.
	 * @param predicate a predicate
	 * @param accepted the consumer to apply when accepted by the specified predicate
	 * @param rejected the consumer to apply when rejected by the specified predicate
	 * @return a consumer that delegates to one of two consumers based on the specified predicate.
	 */
	static IntConsumer when(java.util.function.IntPredicate predicate, java.util.function.IntConsumer accepted, java.util.function.IntConsumer rejected) {
		return new IntConsumer() {
			@Override
			public void accept(int value) {
				java.util.function.IntConsumer consumer = predicate.test(value) ? accepted : rejected;
				consumer.accept(value);
			}
		};
	}

	/**
	 * A consumer that does nothing, ignoring its parameter.
	 */
	class EmptyIntConsumer implements IntConsumer {
		static final IntConsumer INSTANCE = new EmptyIntConsumer();

		private EmptyIntConsumer() {
			// Hide
		}

		@Override
		public void accept(int value) {
			// Do nothing
		}

		@Override
		public IntConsumer andThen(java.util.function.IntConsumer after) {
			return (after instanceof IntConsumer consumer) ? consumer : IntConsumer.of(after, Runner.of());
		}

		@Override
		public Consumer<Integer> box() {
			return Consumer.of();
		}
	}
}
