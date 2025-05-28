/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An enhanced unary consumer.
 * @author Paul Ferraro
 * @param <T> the accepted type
 */
public interface Consumer<T> extends java.util.function.Consumer<T> {

	@Override
	default Consumer<T> andThen(java.util.function.Consumer<? super T> after) {
		return of(List.of(this, after));
	}

	/**
	 * Returns a mapped consumer, that invokes this consumer using result of the specified function.
	 * @param <V> the mapped type
	 * @param mapper a mapping function
	 * @return a mapped consumer
	 */
	default <V> Consumer<V> map(Function<V, T> mapper) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				Consumer.this.accept(mapper.apply(value));
			}
		};
	}

	Consumer<?> EMPTY = new Consumer<>() {
		@Override
		public void accept(Object value) {
		}
	};
	Map<System.Logger.Level, Consumer<Exception>> EXCEPTION_LOGGERS = EnumSet.allOf(System.Logger.Level.class).stream().collect(Collectors.toMap(Function.identity(), ExceptionLogger::new, BinaryOperator.former(), () -> new EnumMap<>(System.Logger.Level.class)));
	Function<System.Logger.Level, Consumer<Exception>> EXCEPTION_LOGGER = EXCEPTION_LOGGERS::get;
	Map<System.Logger.Level, Consumer<AutoCloseable>> SILENT_CLOSERS = EnumSet.allOf(System.Logger.Level.class).stream().collect(Collectors.toMap(Function.identity(), EXCEPTION_LOGGER.andThen(Consumer::close), BinaryOperator.former(), () -> new EnumMap<>(System.Logger.Level.class)));

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
	@SuppressWarnings("unchecked")
	static <V extends AutoCloseable> Consumer<V> close() {
		return (Consumer<V>) close(warning());
	}

	/**
	 * Returns a consumer that logs an exception at the specified level.
	 * @param level the log level
	 * @return an exception logging consumer
	 */
	static Consumer<Exception> log(System.Logger.Level level) {
		return EXCEPTION_LOGGER.apply(level);
	}

	/**
	 * Returns a consumer that logs an exception at the {@link java.lang.System.Logger.Level#ERROR} level.
	 * @return an exception logging consumer
	 */
	static Consumer<Exception> error() {
		return log(System.Logger.Level.ERROR);
	}

	/**
	 * Returns a consumer that logs an exception at the {@link java.lang.System.Logger.Level#WARNING} level.
	 * @return an exception logging consumer
	 */
	static Consumer<Exception> warning() {
		return log(System.Logger.Level.WARNING);
	}

	/**
	 * Returns a consumer that logs an exception at the {@link java.lang.System.Logger.Level#INFO} level.
	 * @return an exception logging consumer
	 */
	static Consumer<Exception> info() {
		return log(System.Logger.Level.INFO);
	}

	/**
	 * Returns a consumer that logs an exception at the {@link java.lang.System.Logger.Level#DEBUG} level.
	 * @return an exception logging consumer
	 */
	static Consumer<Exception> debug() {
		return log(System.Logger.Level.DEBUG);
	}

	/**
	 * Returns a consumer that silently closes its object using the specified exception handler.
	 * @param <V> the auto-closeable type
	 * @param handler an exception handler
	 * @return a silent closing consumer
	 */
	static <V extends AutoCloseable> Consumer<V> close(Consumer<Exception> handler) {
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
	static <V> Consumer<V> of(java.lang.Runnable task) {
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
	static <V> Consumer<V> of(Iterable<java.util.function.Consumer<? super V>> consumers) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				for (java.util.function.Consumer<? super V> consumer : consumers) {
					consumer.accept(value);
				}
			}
		};
	}

	class ExceptionLogger implements Consumer<Exception> {
		private static final System.Logger LOGGER = System.getLogger(Consumer.class.getName());
		private final System.Logger.Level level;

		ExceptionLogger(System.Logger.Level level) {
			this.level = level;
		}

		@Override
		public void accept(Exception exception) {
			if (exception != null) {
				LOGGER.log(this.level, exception.getLocalizedMessage(), exception);
			}
		}
	}
}
