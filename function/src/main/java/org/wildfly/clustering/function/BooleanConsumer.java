/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;

/**
 * A consumer of a boolean value.
 * @author Paul Ferraro
 */
public interface BooleanConsumer extends BooleanOperation, ToVoidOperation {
	/**
	 * Accepts the specified value.
	 * @param value the consumed value
	 */
	void accept(boolean value);

	/**
	 * Returns a consumer that accepts its value this consumer.
	 * @param after another consumer
	 * @return a consumer that accepts its value this consumer.
	 */
	default BooleanConsumer andThen(BooleanConsumer after) {
		return of(List.of(this, after));
	}

	@Override
	default Consumer<Boolean> box() {
		return this.compose(BooleanPredicate.identity().box());
	}

	@Override
	default <V> Consumer<V> compose(java.util.function.Predicate<? super V> before) {
		return Consumer.of(before, this);
	}

	@Override
	default <V1, V2> BiConsumer<V1, V2> composeBinary(java.util.function.BiPredicate<? super V1, ? super V2> before) {
		return BiConsumer.of(before, this);
	}

	@Override
	default BooleanConsumer composeBoolean(BooleanPredicate before) {
		return BooleanConsumer.of(before, this);
	}

	@Override
	default DoubleConsumer composeDouble(java.util.function.DoublePredicate before) {
		return DoubleConsumer.of(before, this);
	}

	@Override
	default IntConsumer composeInt(java.util.function.IntPredicate before) {
		return IntConsumer.of(before, this);
	}

	@Override
	default LongConsumer composeLong(java.util.function.LongPredicate before) {
		return LongConsumer.of(before, this);
	}

	@Override
	default <T> BooleanFunction<T> thenReturn(java.util.function.Supplier<? extends T> after) {
		return BooleanFunction.of(this, after);
	}

	@Override
	default BooleanPredicate thenReturnBoolean(java.util.function.BooleanSupplier after) {
		return BooleanPredicate.of(this, after);
	}

	@Override
	default BooleanToDoubleFunction thenReturnDouble(java.util.function.DoubleSupplier after) {
		return BooleanToDoubleFunction.of(this, after);
	}

	@Override
	default BooleanToIntFunction thenReturnInt(java.util.function.IntSupplier after) {
		return BooleanToIntFunction.of(this, after);
	}

	@Override
	default BooleanToLongFunction thenReturnLong(java.util.function.LongSupplier after) {
		return BooleanToLongFunction.of(this, after);
	}

	@Override
	default BooleanConsumer thenRun(Runnable after) {
		return BooleanConsumer.of(this, after);
	}

	/**
	 * Returns a consumer that does nothing, ignoring its parameter.
	 * @return a consumer that does nothing, ignoring its parameter.
	 */
	static BooleanConsumer of() {
		return EmptyBooleanConsumer.INSTANCE;
	}

	/**
	 * Composes a consumer from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static BooleanConsumer of(BooleanConsumer before, Runnable after) {
		return new BooleanConsumer() {
			@Override
			public void accept(boolean value) {
				before.accept(value);
				after.run();
			}
		};
	}

	/**
	 * Composes a consumer from the specified operations.
	 * @param <T> the intermediate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <T> BooleanConsumer of(BooleanFunction<? extends T> before, java.util.function.Consumer<? super T> after) {
		return new BooleanConsumer() {
			@Override
			public void accept(boolean value) {
				after.accept(before.apply(value));
			}
		};
	}

	/**
	 * Composes a consumer from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static BooleanConsumer of(BooleanPredicate before, BooleanConsumer after) {
		return new BooleanConsumer() {
			@Override
			public void accept(boolean value) {
				after.accept(before.test(value));
			}
		};
	}

	/**
	 * Composes a consumer from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static BooleanConsumer of(BooleanToDoubleFunction before, java.util.function.DoubleConsumer after) {
		return new BooleanConsumer() {
			@Override
			public void accept(boolean value) {
				after.accept(before.applyAsDouble(value));
			}
		};
	}

	/**
	 * Composes a consumer from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static BooleanConsumer of(BooleanToIntFunction before, java.util.function.IntConsumer after) {
		return new BooleanConsumer() {
			@Override
			public void accept(boolean value) {
				after.accept(before.applyAsInt(value));
			}
		};
	}

	/**
	 * Composes a consumer from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static BooleanConsumer of(BooleanToLongFunction before, java.util.function.LongConsumer after) {
		return new BooleanConsumer() {
			@Override
			public void accept(boolean value) {
				after.accept(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a composite consumer that delegates to the specified consumers.
	 * @param consumers a number of consumers
	 * @return a composite consumer
	 */
	static BooleanConsumer of(Iterable<? extends BooleanConsumer> consumers) {
		return new BooleanConsumer() {
			@Override
			public void accept(boolean value) {
				for (BooleanConsumer consumer : consumers) {
					consumer.accept(value);
				}
			}
		};
	}

	/**
	 * A consumer that does nothing, ignoring its parameter.
	 */
	class EmptyBooleanConsumer implements BooleanConsumer {
		static final BooleanConsumer INSTANCE = new EmptyBooleanConsumer();

		private EmptyBooleanConsumer() {
			// Hide
		}

		@Override
		public void accept(boolean value) {
			// Do nothing
		}

		@Override
		public BooleanConsumer andThen(BooleanConsumer after) {
			return after;
		}

		@Override
		public Consumer<Boolean> box() {
			return Consumer.of();
		}
	}
}
