/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;

/**
 * An enhanced long consumer.
 * @author Paul Ferraro
 */
public interface LongConsumer extends java.util.function.LongConsumer, LongOperation, PrimitiveConsumer<Long> {

	@Override
	default LongConsumer andThen(java.util.function.LongConsumer after) {
		return of(List.of(this, after));
	}

	@Override
	default Consumer<Long> box() {
		return this.compose(LongUnaryOperator.identity().box());
	}

	@Override
	default <V> Consumer<V> compose(java.util.function.ToLongFunction<? super V> before) {
		return Consumer.of(before, this);
	}

	@Override
	default <V1, V2> BiConsumer<V1, V2> composeBinary(java.util.function.ToLongBiFunction<? super V1, ? super V2> before) {
		return BiConsumer.of(before, this);
	}

	@Override
	default BooleanConsumer composeBoolean(BooleanToLongFunction before) {
		return BooleanConsumer.of(before, this);
	}

	@Override
	default DoubleConsumer composeDouble(java.util.function.DoubleToLongFunction before) {
		return DoubleConsumer.of(before, this);
	}

	@Override
	default IntConsumer composeInt(java.util.function.IntToLongFunction before) {
		return IntConsumer.of(before, this);
	}

	@Override
	default LongConsumer composeLong(java.util.function.LongUnaryOperator before) {
		return LongConsumer.of(before, this);
	}

	@Override
	default <R> LongFunction<R> thenReturn(java.util.function.Supplier<? extends R> after) {
		return LongFunction.of(this, after);
	}

	@Override
	default LongPredicate thenReturnBoolean(java.util.function.BooleanSupplier after) {
		return LongPredicate.of(this, after);
	}

	@Override
	default LongToDoubleFunction thenReturnDouble(java.util.function.DoubleSupplier after) {
		return LongToDoubleFunction.of(this, after);
	}

	@Override
	default LongToIntFunction thenReturnInt(java.util.function.IntSupplier after) {
		return LongToIntFunction.of(this, after);
	}

	@Override
	default LongUnaryOperator thenReturnLong(java.util.function.LongSupplier after) {
		return LongUnaryOperator.of(this, after);
	}

	@Override
	default LongConsumer thenRun(Runnable after) {
		return LongConsumer.of(this, after);
	}

	@Override
	default LongConsumer thenThrow(java.util.function.Supplier<? extends RuntimeException> exception) {
		return this.thenRun(Runner.of().thenThrow(exception));
	}

	/**
	 * Returns a consumer that ignores its parameter.
	 * @return a consumer that ignores its parameter.
	 */
	static LongConsumer of() {
		return EmptyLongConsumer.INSTANCE;
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static LongConsumer of(java.util.function.LongConsumer before, Runnable after) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
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
	static LongConsumer of(java.util.function.LongPredicate before, BooleanConsumer after) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
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
	static <T> LongConsumer of(java.util.function.LongFunction<? extends T> before, java.util.function.Consumer<? super T> after) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
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
	static LongConsumer of(java.util.function.LongToDoubleFunction before, java.util.function.DoubleConsumer after) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
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
	static LongConsumer of(java.util.function.LongToIntFunction before, java.util.function.IntConsumer after) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
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
	static LongConsumer of(java.util.function.LongUnaryOperator before, java.util.function.LongConsumer after) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
				after.accept(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a composite consumer that delegates to the specified consumers.
	 * @param consumers a number of consumers
	 * @return a composite consumer
	 */
	static LongConsumer of(Iterable<? extends java.util.function.LongConsumer> consumers) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
				for (java.util.function.LongConsumer consumer : consumers) {
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
	static LongConsumer when(java.util.function.LongPredicate predicate, java.util.function.LongConsumer accepted, java.util.function.LongConsumer rejected) {
		return new LongConsumer() {
			@Override
			public void accept(long value) {
				java.util.function.LongConsumer consumer = predicate.test(value) ? accepted : rejected;
				consumer.accept(value);
			}
		};
	}

	/**
	 * A consumer that does nothing, ignoring its parameter.
	 */
	class EmptyLongConsumer implements LongConsumer {
		static final LongConsumer INSTANCE = new EmptyLongConsumer();

		private EmptyLongConsumer() {
			// Hide
		}

		@Override
		public void accept(long value) {
			// Do nothing
		}

		@Override
		public LongConsumer andThen(java.util.function.LongConsumer after) {
			return (after instanceof LongConsumer consumer) ? consumer : LongConsumer.of(after, Runner.of());
		}

		@Override
		public Consumer<Long> box() {
			return Consumer.of();
		}
	}
}
