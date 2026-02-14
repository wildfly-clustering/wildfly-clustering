/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;

/**
 * Runs an operation.
 * @author Paul Ferraro
 */
public interface Runner extends Runnable, VoidOperation, ToVoidOperation {

	@Override
	default Runner compose(Runnable before) {
		return Runner.of(before, this);
	}

	@Override
	default <T> Consumer<T> compose(java.util.function.Consumer<? super T> before) {
		return Consumer.of(before, this);
	}

	@Override
	default <T1, T2> BiConsumer<T1, T2> composeBinary(java.util.function.BiConsumer<? super T1, ? super T2> before) {
		return BiConsumer.of(before, this);
	}

	@Override
	default BooleanConsumer composeBoolean(BooleanConsumer before) {
		return BooleanConsumer.of(before, this);
	}

	@Override
	default DoubleConsumer composeDouble(java.util.function.DoubleConsumer before) {
		return DoubleConsumer.of(before, this);
	}

	@Override
	default IntConsumer composeInt(java.util.function.IntConsumer before) {
		return IntConsumer.of(before, this);
	}

	@Override
	default LongConsumer composeLong(java.util.function.LongConsumer before) {
		return LongConsumer.of(before, this);
	}

	@Override
	default <T> Supplier<T> thenReturn(java.util.function.Supplier<? extends T> after) {
		return Supplier.of(this, after);
	}

	@Override
	default BooleanSupplier thenReturnBoolean(java.util.function.BooleanSupplier after) {
		return BooleanSupplier.of(this, after);
	}

	@Override
	default DoubleSupplier thenReturnDouble(java.util.function.DoubleSupplier after) {
		return DoubleSupplier.of(this, after);
	}

	@Override
	default IntSupplier thenReturnInt(java.util.function.IntSupplier after) {
		return IntSupplier.of(this, after);
	}

	@Override
	default LongSupplier thenReturnLong(java.util.function.LongSupplier after) {
		return LongSupplier.of(this, after);
	}

	@Override
	default Runner thenRun(Runnable after) {
		return Runner.of(this, after);
	}

	/**
	 * Returns an empty runner.
	 * @return an empty runner.
	 */
	static Runner of() {
		return EmptyRunner.INSTANCE;
	}

	/**
	 * Composes a runner from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite runner
	 */
	static Runner of(Runnable before, Runnable after) {
		return of(List.of(before, after));
	}

	/**
	 * Composes a runner from the specified operations.
	 * @param <T> the intermediate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite runner
	 */
	static <T> Runner of(java.util.function.Supplier<? extends T> before, java.util.function.Consumer<? super T> after) {
		return new Runner() {
			@Override
			public void run() {
				after.accept(before.get());
			}
		};
	}

	/**
	 * Composes a runner from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite runner
	 */
	static Runner of(java.util.function.BooleanSupplier before, BooleanConsumer after) {
		return new Runner() {
			@Override
			public void run() {
				after.accept(before.getAsBoolean());
			}
		};
	}

	/**
	 * Composes a runner from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite runner
	 */
	static Runner of(java.util.function.DoubleSupplier before, java.util.function.DoubleConsumer after) {
		return new Runner() {
			@Override
			public void run() {
				after.accept(before.getAsDouble());
			}
		};
	}

	/**
	 * Composes a runner from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite runner
	 */
	static Runner of(java.util.function.IntSupplier before, java.util.function.IntConsumer after) {
		return new Runner() {
			@Override
			public void run() {
				after.accept(before.getAsInt());
			}
		};
	}

	/**
	 * Composes a runner from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite runner
	 */
	static Runner of(java.util.function.LongSupplier before, java.util.function.LongConsumer after) {
		return new Runner() {
			@Override
			public void run() {
				after.accept(before.getAsLong());
			}
		};
	}

	/**
	 * Returns a composite runner that runs the specified runners.
	 * @param runners zero or more runners
	 * @return a composite runner that runs the specified runners, logging any exceptions
	 */
	static Runner of(Iterable<? extends Runnable> runners) {
		return new Runner() {
			@Override
			public void run() {
				for (Runnable runner : runners) {
					runner.run();
				}
			}
		};
	}

	/**
	 * A runner that performs no action.
	 */
	class EmptyRunner implements Runner {
		static final Runner INSTANCE = new EmptyRunner();

		private EmptyRunner() {
			// Hide
		}

		@Override
		public void run() {
			// Do nothing
		}

		@Override
		public Runner thenRun(Runnable after) {
			return (after instanceof Runner runner) ? runner : of(List.of(after));
		}

		@Override
		public Runner compose(Runnable before) {
			return (before instanceof Runner runner) ? runner : of(List.of(before));
		}
	}
}
