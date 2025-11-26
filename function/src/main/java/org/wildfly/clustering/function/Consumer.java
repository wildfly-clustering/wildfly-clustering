/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An enhanced unary consumer.
 * @author Paul Ferraro
 * @param <T> the accepted type
 */
public interface Consumer<T> extends java.util.function.Consumer<T> {

	@Override
	default Consumer<T> andThen(java.util.function.Consumer<? super T> after) {
		return acceptAll(List.<java.util.function.Consumer<? super T>>of(this, after));
	}

	/**
	 * Returns a consumer that conditionally invokes this consumer when allowed by the specified predicate.
	 * @param predicate a predicate that determines whether or not to invoke this consumer
	 * @return a consumer that conditionally invokes this consumer when allowed by the specified predicate.
	 */
	default Consumer<T> when(java.util.function.Predicate<T> predicate) {
		return new Consumer<>() {
			@Override
			public void accept(T value) {
				if (predicate.test(value)) {
					Consumer.this.accept(value);
				}
			}
		};
	}

	/**
	 * Returns a consumer that accepts the value returned by the specified default provider if its value does not match the specified predicate.
	 * @param predicate a predicate used to determine the parameter of this consumer
	 * @param defaultValue a provider of the default parameter value
	 * @return a consumer that accepts the value returned by the specified default provider if its value does not match the specified predicate.
	 */
	default Consumer<T> withDefault(java.util.function.Predicate<T> predicate, java.util.function.Supplier<T> defaultValue) {
		return new Consumer<>() {
			@Override
			public void accept(T value) {
				Consumer.this.accept(predicate.test(value) ? value : defaultValue.get());
			}
		};
	}

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param <V> the mapped type
	 * @param mapper a mapping function
	 * @return a mapped consumer
	 */
	default <V> Consumer<V> compose(java.util.function.Function<V, T> mapper) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				Consumer.this.accept(mapper.apply(value));
			}
		};
	}

	/**
	 * Composes a binary consumer that invokes this consumer using result of the specified binary function.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param mapper a mapping function
	 * @return a binary consumer that invokes this consumer using result of the specified binary function.
	 */
	default <V1, V2> BiConsumer<V1, V2> compose(java.util.function.BiFunction<V1, V2, T> mapper) {
		return new BiConsumer<>() {
			@Override
			public void accept(V1 value1, V2 value2) {
				Consumer.this.accept(mapper.apply(value1, value2));
			}
		};
	}

	/**
	 * Returns a new consumer that delegates to the specified handler in the event of an exception.
	 * @param handler an exception handler
	 * @return a new consumer that delegates to the specified handler in the event of an exception.
	 */
	default Consumer<T> handle(java.util.function.BiConsumer<T, RuntimeException> handler) {
		return new Consumer<>() {
			@Override
			public void accept(T value) {
				try {
					Consumer.this.accept(value);
				} catch (RuntimeException e) {
					handler.accept(value, e);
				}
			}
		};
	}

	/**
	 * Returns a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 * @param factory a factory of the function return value
	 * @param <R> the return type
	 * @return a function that returns the value from the specified supplier after accepting its parameter via this consumer.
	 */
	default <R> Function<T, R> thenReturn(java.util.function.Supplier<R> factory) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				Consumer.this.accept(value);
				return factory.get();
			}
		};
	}

	/**
	 * Returns a new consumer that accepts its value while holding the monitor returned by the specified function.
	 * @param <M> a function returning an object monitor
	 * @param monitorFunction a function returning an object monitor.
	 * @return a new consumer that accepts its value while holding the monitor returned by the specified function.
	 */
	default <M> Consumer<T> withMonitor(java.util.function.Function<T, M> monitorFunction) {
		return new Consumer<>() {
			@Override
			public void accept(T value) {
				M monitor = monitorFunction.apply(value);
				if (monitor != null) {
					synchronized (monitor) {
						Consumer.this.accept(value);
					}
				} else {
					Consumer.this.accept(value);
				}
			}
		};
	}

	/** A consumer that does nothing with its parameter */
	Consumer<?> EMPTY = value -> {};
	/** A map of exception logging consumers per level */
	Map<System.Logger.Level, Consumer<Exception>> EXCEPTION_LOGGERS = EnumSet.allOf(System.Logger.Level.class).stream().collect(Collectors.toMap(Function.identity(), ExceptionLogger::new, BinaryOperator.former(), Supplier.of(System.Logger.Level.class).thenApply(EnumMap::new)));
	/** A function returning the exception logger for a given level */
	Function<System.Logger.Level, Consumer<Exception>> EXCEPTION_LOGGER = EXCEPTION_LOGGERS::get;

	/**
	 * Returns a consumer that performs no action.
	 * @param <V> the consumed type
	 * @return an empty consumer
	 */
	@SuppressWarnings("unchecked")
	static <V> Consumer<V> empty() {
		return (Consumer<V>) EMPTY;
	}

	/**
	 * Returns a consumer that silently closes its object.
	 * @param <V> the auto-closeable type
	 * @return an closing consumer
	 */
	static <V extends AutoCloseable> Consumer<V> close() {
		return close(warning());
	}

	/**
	 * Returns a consumer that logs an exception at the specified level.
	 * @param level the log level
	 * @param <E> the exception type
	 * @return an exception logging consumer
	 */
	@SuppressWarnings("unchecked")
	static <E extends Exception> Consumer<E> log(System.Logger.Level level) {
		return (Consumer<E>) (Consumer<?>) EXCEPTION_LOGGER.apply(level);
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
	 * Returns a consumer that silently closes its object using the specified exception handler.
	 * @param <V> the auto-closeable type
	 * @param handler an exception handler
	 * @return a silent closing consumer
	 */
	static <V extends AutoCloseable> Consumer<V> close(java.util.function.Consumer<Exception> handler) {
		return new Consumer<>() {
			@Override
			public void accept(AutoCloseable object) {
				if (object != null) {
					try {
						object.close();
					} catch (Exception e) {
						handler.accept(e);
					}
				}
			}
		};
	}

	/**
	 * Returns a consumer that runs the specified task, ignoring its parameter.
	 * @param <V> the ignored parameter type
	 * @param task a runnable task
	 * @return a consumer that runs the specified task, ignoring its parameter.
	 */
	static <V> Consumer<V> run(java.lang.Runnable task) {
		return new Consumer<>() {
			@Override
			public void accept(V ignored) {
				task.run();
			}
		};
	}

	/**
	 * Returns a composite consumer that delegates to zero or more consumers.
	 * @param <V> the consumed type
	 * @param consumers zero or more consumers
	 * @return a composite consumer
	 */
	static <V> Consumer<V> acceptAll(Iterable<? extends java.util.function.Consumer<? super V>> consumers) {
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
	 * Returns a consumer that wraps an exception as a runtime exception via the specified factory.
	 * @param <E> the exception type
	 * @param exceptionFactory a runtime exception wrapper
	 * @return a consumer that wraps an exception as a runtime exception via the specified factory.
	 */
	static <E extends Throwable> Consumer<E> throwing(java.util.function.Function<E, ? extends RuntimeException> exceptionFactory) {
		return new Consumer<>() {
			@Override
			public void accept(E exception) {
				throw exceptionFactory.apply(exception);
			}
		};
	}

	/**
	 * A consumer of an exception that logs its parameter.
	 * @param <E> the exception type
	 */
	class ExceptionLogger<E extends Exception> implements Consumer<E> {
		private static final System.Logger LOGGER = System.getLogger(Consumer.class.getName());
		private final System.Logger.Level level;

		ExceptionLogger(System.Logger.Level level) {
			this.level = level;
		}

		@Override
		public void accept(E exception) {
			if (exception != null) {
				LOGGER.log(this.level, exception.getLocalizedMessage(), exception);
			}
		}
	}
}
