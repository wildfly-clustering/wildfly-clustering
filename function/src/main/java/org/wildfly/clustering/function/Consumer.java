/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * A consumer accepting a single parameter.
 * @author Paul Ferraro
 * @param <V> the accepted type
 */
public interface Consumer<V> extends java.util.function.Consumer<V>, ObjectOperation<V>, ToVoidOperation {

	@Override
	default Consumer<V> andThen(java.util.function.Consumer<? super V> after) {
		return new CompositeConsumer<V>(List.of(this, after));
	}

	@Override
	default <T> Consumer<T> compose(java.util.function.Function<? super T, ? extends V> before) {
		return Consumer.of(before, this);
	}

	@Override
	default <T1, T2> BiConsumer<T1, T2> composeBinary(java.util.function.BiFunction<? super T1, ? super T2, ? extends V> before) {
		return BiConsumer.of(before, this);
	}

	@Override
	default BooleanConsumer composeBoolean(BooleanFunction<? extends V> before) {
		return BooleanConsumer.of(before, this);
	}

	@Override
	default DoubleConsumer composeDouble(java.util.function.DoubleFunction<? extends V> before) {
		return DoubleConsumer.of(before, this);
	}

	@Override
	default IntConsumer composeInt(java.util.function.IntFunction<? extends V> before) {
		return IntConsumer.of(before, this);
	}

	@Override
	default LongConsumer composeLong(java.util.function.LongFunction<? extends V> before) {
		return LongConsumer.of(before, this);
	}

	@Override
	default <R> Function<V, R> thenReturn(java.util.function.Supplier<? extends R> factory) {
		return Function.of(this, factory);
	}

	@Override
	default Predicate<V> thenReturnBoolean(java.util.function.BooleanSupplier after) {
		return Predicate.of(this, after);
	}

	@Override
	default ToDoubleFunction<V> thenReturnDouble(java.util.function.DoubleSupplier after) {
		return ToDoubleFunction.of(this, after);
	}

	@Override
	default ToIntFunction<V> thenReturnInt(java.util.function.IntSupplier after) {
		return ToIntFunction.of(this, after);
	}

	@Override
	default ToLongFunction<V> thenReturnLong(java.util.function.LongSupplier after) {
		return ToLongFunction.of(this, after);
	}

	@Override
	default Consumer<V> thenRun(Runnable after) {
		return Consumer.of(this, after);
	}

	@Override
	default Consumer<V> thenThrow(java.util.function.Supplier<? extends RuntimeException> exception) {
		return this.thenRun(Runner.of().thenThrow(exception));
	}

	/**
	 * Returns a consumer that performs no action.
	 * @param <V> the consumed type
	 * @return an empty consumer
	 */
	@SuppressWarnings("unchecked")
	static <V> Consumer<V> of() {
		return (Consumer<V>) EmptyConsumer.INSTANCE;
	}

	/**
	 * Returns a consumer that silently closes its object.
	 * @param <V> the auto-closeable type
	 * @return an closing consumer
	 */
	@SuppressWarnings("unchecked")
	static <V extends AutoCloseable> Consumer<V> close() {
		return (Consumer<V>) AutoCloseableConsumer.WARN;
	}

	/**
	 * Returns a consumer that silently closes its object using the specified exception handler.
	 * @param <V> the auto-closeable type
	 * @param handler an exception handler
	 * @return a silent closing consumer
	 */
	static <V extends AutoCloseable> Consumer<V> close(java.util.function.Consumer<? super Exception> handler) {
		return new AutoCloseableConsumer<>(handler);
	}

	/**
	 * Returns a consumer that silently closes its object using the specified exception handler.
	 * @param <V> the auto-closeable type
	 * @param handler an exception handler
	 * @return a silent closing consumer
	 */
	static <V extends AutoCloseable> Consumer<V> close(java.util.function.BiConsumer<? super V, ? super Exception> handler) {
		return new AutoCloseableConsumer<>(handler);
	}

	/**
	 * Returns a consumer that logs an exception at the specified level.
	 * @param level the log level
	 * @param <E> the exception type
	 * @return an exception logging consumer
	 */
	static <E extends Exception> Consumer<E> log(System.Logger.Level level) {
		return ExceptionLogger.getLogger(level);
	}

	/**
	 * Returns a consumer that logs an exception at the {@link java.lang.System.Logger.Level#ERROR} level.
	 * @param <E> the exception type
	 * @return an exception logging consumer
	 */
	static <E extends Exception> Consumer<E> error() {
		return log(System.Logger.Level.ERROR);
	}

	/**
	 * Returns a consumer that logs an exception at the {@link java.lang.System.Logger.Level#WARNING} level.
	 * @param <E> the exception type
	 * @return an exception logging consumer
	 */
	static <E extends Exception> Consumer<E> warning() {
		return log(System.Logger.Level.WARNING);
	}

	/**
	 * Returns a consumer that logs an exception at the {@link java.lang.System.Logger.Level#INFO} level.
	 * @param <E> the exception type
	 * @return an exception logging consumer
	 */
	static <E extends Exception> Consumer<E> info() {
		return log(System.Logger.Level.INFO);
	}

	/**
	 * Returns a consumer that logs an exception at the {@link java.lang.System.Logger.Level#DEBUG} level.
	 * @param <E> the exception type
	 * @return an exception logging consumer
	 */
	static <E extends Exception> Consumer<E> debug() {
		return log(System.Logger.Level.DEBUG);
	}

	/**
	 * Returns a consumer combining the specified operations.
	 * @param <V> the consumed type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <V> Consumer<V> of(java.util.function.Consumer<? super V> before, java.lang.Runnable after) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				before.accept(value);
				after.run();
			}
		};
	}

	/**
	 * Returns a consumer combining the specified operations.
	 * @param <V> the consumed type
	 * @param <R> the intermediate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <V, R> Consumer<V> of(java.util.function.Function<? super V, ? extends R> before, java.util.function.Consumer<? super R> after) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				after.accept(before.apply(value));
			}
		};
	}

	/**
	 * Returns a consumer combining the specified operations.
	 * @param <V> the consumed type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <V> Consumer<V> of(java.util.function.Predicate<? super V> before, BooleanConsumer after) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				after.accept(before.test(value));
			}
		};
	}

	/**
	 * Returns a consumer combining the specified operations.
	 * @param <V> the consumed type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <V> Consumer<V> of(java.util.function.ToDoubleFunction<? super V> before, java.util.function.DoubleConsumer after) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				after.accept(before.applyAsDouble(value));
			}
		};
	}

	/**
	 * Returns a consumer combining the specified operations.
	 * @param <V> the consumed type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <V> Consumer<V> of(java.util.function.ToIntFunction<? super V> before, java.util.function.IntConsumer after) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				after.accept(before.applyAsInt(value));
			}
		};
	}

	/**
	 * Returns a consumer combining the specified operations.
	 * @param <V> the consumed type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite consumer
	 */
	static <V> Consumer<V> of(java.util.function.ToLongFunction<? super V> before, java.util.function.LongConsumer after) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				after.accept(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a composite consumer that delegates to zero or more consumers.
	 * @param <V> the consumed type
	 * @param consumers zero or more consumers
	 * @return a composite consumer
	 */
	static <V> Consumer<V> of(Iterable<? extends java.util.function.Consumer<? super V>> consumers) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				for (java.util.function.Consumer<? super V> consumer : consumers) {
					consumer.accept(value);
				}
			}
		};
	}

	/**
	 * Returns a consumer that delegates to one of two consumers based on the specified predicate.
	 * @param <T> the consumer parameter type
	 * @param predicate a predicate
	 * @param accepted the consumer to apply when accepted by the specified predicate
	 * @param rejected the consumer to apply when rejected by the specified predicate
	 * @return a consumer that delegates to one of two consumers based on the specified predicate.
	 */
	static <T> Consumer<T> when(java.util.function.Predicate<? super T> predicate, java.util.function.Consumer<? super T> accepted, java.util.function.Consumer<? super T> rejected) {
		return new Consumer<>() {
			@Override
			public void accept(T value) {
				java.util.function.Consumer<? super T> consumer = predicate.test(value) ? accepted : rejected;
				consumer.accept(value);
			}
		};
	}

	/**
	 * A consumer of an exception that logs its parameter.
	 * @param <E> the exception type
	 */
	class ExceptionLogger<E extends Exception> implements Consumer<E> {
		private static final System.Logger LOGGER = System.getLogger(Consumer.class.getName());
		private static final Map<System.Logger.Level, Consumer<Exception>> EXCEPTION_LOGGERS = new EnumMap<>(System.Logger.Level.class);
		static {
			for (System.Logger.Level level : EnumSet.allOf(System.Logger.Level.class)) {
				EXCEPTION_LOGGERS.put(level, new ExceptionLogger<>(level));
			}
		}

		private final System.Logger.Level level;

		ExceptionLogger(System.Logger.Level level) {
			this.level = level;
		}

		@SuppressWarnings("unchecked")
		static <E extends Exception> Consumer<E> getLogger(System.Logger.Level level) {
			return (Consumer<E>) EXCEPTION_LOGGERS.get(level);
		}

		@Override
		public void accept(E exception) {
			if (exception != null) {
				LOGGER.log(this.level, exception.getLocalizedMessage(), exception);
			}
		}
	}

	/**
	 * A consumer that does nothing, ignoring its parameter.
	 * @param <T> the consumed type
	 */
	class EmptyConsumer<T> implements Consumer<T> {
		static final Consumer<?> INSTANCE = new EmptyConsumer<>();

		private EmptyConsumer() {
			// Hide
		}

		@Override
		public void accept(T ignore) {
			// Do nothing
		}

		@SuppressWarnings("unchecked")
		@Override
		public Consumer<T> andThen(java.util.function.Consumer<? super T> after) {
			return (after instanceof Consumer<? super T> consumer) ? (Consumer<T>) consumer : after::accept;
		}
	}

	/**
	 * An auto-closing consumer.
	 * @param <T> the auto-closable type
	 */
	class AutoCloseableConsumer<T extends AutoCloseable> implements Consumer<T> {
		static final Consumer<AutoCloseable> WARN = new AutoCloseableConsumer<>(warning());

		private final java.util.function.BiConsumer<? super T, ? super Exception> handler;

		AutoCloseableConsumer(java.util.function.Consumer<? super Exception> handler) {
			this.handler = BiConsumer.of(Consumer.of(), handler);
		}

		AutoCloseableConsumer(java.util.function.BiConsumer<? super T, ? super Exception> handler) {
			this.handler = handler;
		}

		@Override
		public void accept(T value) {
			if (value != null) {
				try {
					value.close();
				} catch (Exception e) {
					this.handler.accept(value, e);
				}
			}
		}
	}

	/**
	 * A composite consumer.
	 * @param <T> the consumed type
	 */
	class CompositeConsumer<T> implements Consumer<T> {
		private final Iterable<? extends java.util.function.Consumer<? super T>> consumers;

		CompositeConsumer(Iterable<? extends java.util.function.Consumer<? super T>> consumers) {
			this.consumers = consumers;
		}

		@Override
		public void accept(T value) {
			for (java.util.function.Consumer<? super T> consumer : this.consumers) {
				consumer.accept(value);
			}
		}
	}
}
