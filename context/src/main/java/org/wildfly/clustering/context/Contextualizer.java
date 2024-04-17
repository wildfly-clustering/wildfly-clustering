/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.wildfly.common.function.ExceptionBiConsumer;
import org.wildfly.common.function.ExceptionBiFunction;
import org.wildfly.common.function.ExceptionConsumer;
import org.wildfly.common.function.ExceptionFunction;
import org.wildfly.common.function.ExceptionRunnable;
import org.wildfly.common.function.ExceptionSupplier;

/**
 * Facility for creating contextual tasks.
 * @author Paul Ferraro
 */
public interface Contextualizer {

	/**
	 * Decorates the specified runner with a given context.
	 * @param runner a runnable task
	 * @return a contextual runner
	 */
	Runnable contextualize(Runnable runner);

	/**
	 * Decorates the specified runner with a given context.
	 * @param <E> the exception type
	 * @param runner a runnable task
	 * @return a contextual runner
	 */
	<E extends Exception> ExceptionRunnable<E> contextualize(ExceptionRunnable<E> runner);

	/**
	 * Decorates the specified caller with a given context.
	 * @param <T> the return type
	 * @param caller a callable task
	 * @return a contextual caller
	 */
	<T> Callable<T> contextualize(Callable<T> caller);

	/**
	 * Decorates the specified supplier with a given context.
	 * @param <T> the return type
	 * @param supplier a supplier task
	 * @return a contextual supplier
	 */
	<T> Supplier<T> contextualize(Supplier<T> supplier);

	/**
	 * Decorates the specified supplier with a given context.
	 * @param <T> the return type
	 * @param <E> the exception type
	 * @param supplier a supplier task
	 * @return a contextual supplier
	 */
	<T, E extends Exception> ExceptionSupplier<T, E> contextualize(ExceptionSupplier<T, E> supplier);

	/**
	 * Decorates the specified consumer with a given context.
	 * @param <V> the consumed value type
	 * @param consumer a consumer
	 * @return a contextual consumer
	 */
	<V> Consumer<V> contextualize(Consumer<V> consumer);

	/**
	 * Decorates the specified consumer with a given context.
	 * @param <V> the consumed value type
	 * @param <E> the exception type
	 * @param consumer a consumer
	 * @return a contextual consumer
	 */
	<V, E extends Exception> ExceptionConsumer<V, E> contextualize(ExceptionConsumer<V, E> consumer);

	/**
	 * Decorates the specified consumer with a given context.
	 * @param <V1> the 1st consumed value type
	 * @param <V2> the 2nd consumed value type
	 * @param consumer a consumer
	 * @return a contextual consumer
	 */
	<V1, V2> BiConsumer<V1, V2> contextualize(BiConsumer<V1, V2> consumer);

	/**
	 * Decorates the specified consumer with a given context.
	 * @param <V1> the 1st consumed value type
	 * @param <V2> the 2nd consumed value type
	 * @param <E> the exception type
	 * @param consumer a consumer
	 * @return a contextual consumer
	 */
	<V1, V2, E extends Exception> ExceptionBiConsumer<V1, V2, E> contextualize(ExceptionBiConsumer<V1, V2, E> consumer);

	/**
	 * Decorates the specified function with a given context.
	 * @param <V> the function parameter type
	 * @param <R> the function return type
	 * @param function a function
	 * @return a contextual function
	 */
	<V, R> Function<V, R> contextualize(Function<V, R> function);

	/**
	 * Decorates the specified function with a given context.
	 * @param <V> the function parameter type
	 * @param <R> the function return type
	 * @param <E> the exception type
	 * @param function a function
	 * @return a contextual function
	 */
	<V, R, E extends Exception> ExceptionFunction<V, R, E> contextualize(ExceptionFunction<V, R, E> function);

	/**
	 * Decorates the specified function with a given context.
	 * @param <V1> the 1st function parameter type
	 * @param <V2> the 2nd function parameter type
	 * @param <R> the function return type
	 * @param function a function
	 * @return a contextual function
	 */
	<V1, V2, R> BiFunction<V1, V2, R> contextualize(BiFunction<V1, V2, R> function);

	/**
	 * Decorates the specified function with a given context.
	 * @param <V1> the 1st function parameter type
	 * @param <V2> the 2nd function parameter type
	 * @param <R> the function return type
	 * @param <E> the exception type
	 * @param function a function
	 * @return a contextual function
	 */
	<V1, V2, R, E extends Exception> ExceptionBiFunction<V1, V2, R, E> contextualize(ExceptionBiFunction<V1, V2, R, E> function);

	Contextualizer NONE = new Contextualizer() {

		@Override
		public Runnable contextualize(Runnable runner) {
			return runner;
		}

		@Override
		public <E extends Exception> ExceptionRunnable<E> contextualize(ExceptionRunnable<E> runner) {
			return runner;
		}

		@Override
		public <T> Callable<T> contextualize(Callable<T> caller) {
			return caller;
		}

		@Override
		public <T> Supplier<T> contextualize(Supplier<T> supplier) {
			return supplier;
		}

		@Override
		public <T, E extends Exception> ExceptionSupplier<T, E> contextualize(ExceptionSupplier<T, E> supplier) {
			return supplier;
		}

		@Override
		public <V> Consumer<V> contextualize(Consumer<V> consumer) {
			return consumer;
		}

		@Override
		public <V, E extends Exception> ExceptionConsumer<V, E> contextualize(ExceptionConsumer<V, E> consumer) {
			return consumer;
		}

		@Override
		public <V1, V2> BiConsumer<V1, V2> contextualize(BiConsumer<V1, V2> consumer) {
			return consumer;
		}

		@Override
		public <V1, V2, E extends Exception> ExceptionBiConsumer<V1, V2, E> contextualize(ExceptionBiConsumer<V1, V2, E> consumer) {
			return consumer;
		}

		@Override
		public <V, R> Function<V, R> contextualize(Function<V, R> function) {
			return function;
		}

		@Override
		public <V, R, E extends Exception> ExceptionFunction<V, R, E> contextualize(ExceptionFunction<V, R, E> function) {
			return function;
		}

		@Override
		public <V1, V2, R> BiFunction<V1, V2, R> contextualize(BiFunction<V1, V2, R> function) {
			return function;
		}

		@Override
		public <V1, V2, R, E extends Exception> ExceptionBiFunction<V1, V2, R, E> contextualize(ExceptionBiFunction<V1, V2, R, E> function) {
			return function;
		}
	};

	static <C> Contextualizer withContext(C context, ContextReference<C> reference) {
		ContextualExecutor executor = ContextualExecutor.withContext(context, reference);
		return new Contextualizer() {
			@Override
			public Runnable contextualize(Runnable runner) {
				return new Runnable() {
					@Override
					public void run() {
						executor.execute(runner);
					}
				};
			}

			@Override
			public <E extends Exception> ExceptionRunnable<E> contextualize(ExceptionRunnable<E> runner) {
				return new ExceptionRunnable<>() {
					@Override
					public void run() throws E {
						executor.execute(runner);
					}
				};
			}

			@Override
			public <T> Callable<T> contextualize(Callable<T> caller) {
				return new Callable<>() {
					@Override
					public T call() throws Exception {
						return executor.execute(caller);
					}
				};
			}

			@Override
			public <T> Supplier<T> contextualize(Supplier<T> supplier) {
				return new Supplier<>() {
					@Override
					public T get() {
						return executor.execute(supplier);
					}
				};
			}

			@Override
			public <T, E extends Exception> ExceptionSupplier<T, E> contextualize(ExceptionSupplier<T, E> supplier) {
				return new ExceptionSupplier<>() {
					@Override
					public T get() throws E {
						return executor.execute(supplier);
					}
				};
			}

			@Override
			public <V> Consumer<V> contextualize(Consumer<V> consumer) {
				return new Consumer<>() {
					@Override
					public void accept(V value) {
						executor.execute(consumer, value);
					}
				};
			}

			@Override
			public <V, E extends Exception> ExceptionConsumer<V, E> contextualize(ExceptionConsumer<V, E> consumer) {
				return new ExceptionConsumer<>() {
					@Override
					public void accept(V value) throws E {
						executor.execute(consumer, value);
					}
				};
			}

			@Override
			public <V1, V2> BiConsumer<V1, V2> contextualize(BiConsumer<V1, V2> consumer) {
				return new BiConsumer<>() {
					@Override
					public void accept(V1 value1, V2 value2) {
						executor.execute(consumer, value1, value2);
					}
				};
			}

			@Override
			public <V1, V2, E extends Exception> ExceptionBiConsumer<V1, V2, E> contextualize(ExceptionBiConsumer<V1, V2, E> consumer) {
				return new ExceptionBiConsumer<>() {
					@Override
					public void accept(V1 value1, V2 value2) throws E {
						executor.execute(consumer, value1, value2);
					}
				};
			}

			@Override
			public <V, R> Function<V, R> contextualize(Function<V, R> function) {
				return new Function<>() {
					@Override
					public R apply(V value) {
						return executor.execute(function, value);
					}
				};
			}

			@Override
			public <V, R, E extends Exception> ExceptionFunction<V, R, E> contextualize(ExceptionFunction<V, R, E> function) {
				return new ExceptionFunction<>() {
					@Override
					public R apply(V value) throws E {
						return executor.execute(function, value);
					}
				};
			}

			@Override
			public <V1, V2, R> BiFunction<V1, V2, R> contextualize(BiFunction<V1, V2, R> function) {
				return new BiFunction<>() {
					@Override
					public R apply(V1 value1, V2 value2) {
						return executor.execute(function, value1, value2);
					}
				};
			}

			@Override
			public <V1, V2, R, E extends Exception> ExceptionBiFunction<V1, V2, R, E> contextualize(ExceptionBiFunction<V1, V2, R, E> function) {
				return new ExceptionBiFunction<>() {
					@Override
					public R apply(V1 value1, V2 value2) throws E {
						return executor.execute(function, value1, value2);
					}
				};
			}
		};
	}

	static Contextualizer composite(List<Contextualizer> contextualizers) {
		return new Contextualizer() {
			@Override
			public Runnable contextualize(Runnable runner) {
				Runnable result = runner;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}

			@Override
			public <E extends Exception> ExceptionRunnable<E> contextualize(ExceptionRunnable<E> runner) {
				ExceptionRunnable<E> result = runner;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}

			@Override
			public <T> Callable<T> contextualize(Callable<T> caller) {
				Callable<T> result = caller;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}

			@Override
			public <T> Supplier<T> contextualize(Supplier<T> supplier) {
				Supplier<T> result = supplier;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}

			@Override
			public <T, E extends Exception> ExceptionSupplier<T, E> contextualize(ExceptionSupplier<T, E> supplier) {
				ExceptionSupplier<T, E> result = supplier;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}

			@Override
			public <V> Consumer<V> contextualize(Consumer<V> consumer) {
				Consumer<V> result = consumer;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}

			@Override
			public <V, E extends Exception> ExceptionConsumer<V, E> contextualize(ExceptionConsumer<V, E> consumer) {
				ExceptionConsumer<V, E> result = consumer;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}

			@Override
			public <V1, V2> BiConsumer<V1, V2> contextualize(BiConsumer<V1, V2> consumer) {
				BiConsumer<V1, V2> result = consumer;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}

			@Override
			public <V1, V2, E extends Exception> ExceptionBiConsumer<V1, V2, E> contextualize(ExceptionBiConsumer<V1, V2, E> consumer) {
				ExceptionBiConsumer<V1, V2, E> result = consumer;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}

			@Override
			public <V, R> Function<V, R> contextualize(Function<V, R> function) {
				Function<V, R> result = function;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}

			@Override
			public <V, R, E extends Exception> ExceptionFunction<V, R, E> contextualize(ExceptionFunction<V, R, E> function) {
				ExceptionFunction<V, R, E> result = function;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}

			@Override
			public <V1, V2, R> BiFunction<V1, V2, R> contextualize(BiFunction<V1, V2, R> function) {
				BiFunction<V1, V2, R> result = function;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}

			@Override
			public <V1, V2, R, E extends Exception> ExceptionBiFunction<V1, V2, R, E> contextualize(ExceptionBiFunction<V1, V2, R, E> function) {
				ExceptionBiFunction<V1, V2, R, E> result = function;
				for (Contextualizer contextualizer : contextualizers) {
					result = contextualizer.contextualize(result);
				}
				return result;
			}
		};
	}
}
