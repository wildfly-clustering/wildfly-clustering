/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;

/**
 * A consumer accepting a double parameter.
 * @author Paul Ferraro
 */
public interface DoubleConsumer extends java.util.function.DoubleConsumer, DoubleOperation, PrimitiveConsumer<Double> {

	@Override
	default DoubleConsumer andThen(java.util.function.DoubleConsumer after) {
		return of(List.of(this, after));
	}

	@Override
	default Consumer<Double> box() {
		return this.compose(DoubleUnaryOperator.identity().box());
	}

	@Override
	default <V> Consumer<V> compose(java.util.function.ToDoubleFunction<? super V> before) {
		return Consumer.of(before, this);
	}

	@Override
	default <V1, V2> BiConsumer<V1, V2> composeBinary(java.util.function.ToDoubleBiFunction<? super V1, ? super V2> before) {
		return BiConsumer.of(before, this);
	}

	@Override
	default BooleanConsumer composeBoolean(BooleanToDoubleFunction before) {
		return BooleanConsumer.of(before, this);
	}

	@Override
	default DoubleConsumer composeDouble(java.util.function.DoubleUnaryOperator before) {
		return DoubleConsumer.of(before, this);
	}

	@Override
	default IntConsumer composeInt(java.util.function.IntToDoubleFunction before) {
		return IntConsumer.of(before, this);
	}

	@Override
	default LongConsumer composeLong(java.util.function.LongToDoubleFunction before) {
		return LongConsumer.of(before, this);
	}

	@Override
	default <R> DoubleFunction<R> thenReturn(java.util.function.Supplier<? extends R> after) {
		return DoubleFunction.of(this, after);
	}

	@Override
	default DoublePredicate thenReturnBoolean(java.util.function.BooleanSupplier after) {
		return DoublePredicate.of(this, after);
	}

	@Override
	default DoubleUnaryOperator thenReturnDouble(java.util.function.DoubleSupplier after) {
		return DoubleUnaryOperator.of(this, after);
	}

	@Override
	default DoubleToIntFunction thenReturnInt(java.util.function.IntSupplier after) {
		return DoubleToIntFunction.of(this, after);
	}

	@Override
	default DoubleToLongFunction thenReturnLong(java.util.function.LongSupplier after) {
		return DoubleToLongFunction.of(this, after);
	}

	@Override
	default DoubleConsumer thenRun(Runnable after) {
		return DoubleConsumer.of(this, after);
	}

	/**
	 * Returns a consumer that ignores its parameter.
	 * @return a consumer that ignores its parameter.
	 */
	static DoubleConsumer of() {
		return EmptyDoubleConsumer.INSTANCE;
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static DoubleConsumer of(java.util.function.DoubleConsumer before, Runnable after) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
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
	static DoubleConsumer of(java.util.function.DoublePredicate before, BooleanConsumer after) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
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
	static <T> DoubleConsumer of(java.util.function.DoubleFunction<? extends T> before, java.util.function.Consumer<? super T> after) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
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
	static DoubleConsumer of(java.util.function.DoubleUnaryOperator before, java.util.function.DoubleConsumer after) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
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
	static DoubleConsumer of(java.util.function.DoubleToIntFunction before, java.util.function.IntConsumer after) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
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
	static DoubleConsumer of(java.util.function.DoubleToLongFunction before, java.util.function.LongConsumer after) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
				after.accept(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a composite consumer that delegates to the specified consumers.
	 * @param consumers a number of consumers
	 * @return a composite consumer
	 */
	static DoubleConsumer of(Iterable<? extends java.util.function.DoubleConsumer> consumers) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
				for (java.util.function.DoubleConsumer consumer : consumers) {
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
	static DoubleConsumer when(java.util.function.DoublePredicate predicate, java.util.function.DoubleConsumer accepted, java.util.function.DoubleConsumer rejected) {
		return new DoubleConsumer() {
			@Override
			public void accept(double value) {
				java.util.function.DoubleConsumer consumer = predicate.test(value) ? accepted : rejected;
				consumer.accept(value);
			}
		};
	}

	/**
	 * A consumer that does nothing, ignoring its parameter.
	 */
	class EmptyDoubleConsumer implements DoubleConsumer {
		static final DoubleConsumer INSTANCE = new EmptyDoubleConsumer();

		private EmptyDoubleConsumer() {
			// Hide
		}

		@Override
		public void accept(double value) {
			// Do nothing
		}

		@Override
		public DoubleConsumer andThen(java.util.function.DoubleConsumer after) {
			return (after instanceof DoubleConsumer consumer) ? consumer : DoubleConsumer.of(after, Runner.of());
		}

		@Override
		public Consumer<Double> box() {
			return Consumer.of();
		}
	}
}
