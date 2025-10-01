/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Facility for contextual execution.
 * @author Paul Ferraro
 */
public interface ContextualExecutor extends Executor {

	/**
	 * Executes the specified consumer using the specified value.
	 * @param <V> the consumed value type
	 * @param consumer a consumer
	 * @param value the consumed value
	 */
	<V> void execute(Consumer<V> consumer, V value);

	/**
	 * Executes the specified runner.
	 * @param <V1> the 1st consumed value type
	 * @param <V2> the 2nd consumed value type
	 * @param consumer a consumer
	 * @param value1 the 1st consumed value
	 * @param value2 the 2nd consumed value
	 */
	<V1, V2> void execute(BiConsumer<V1, V2> consumer, V1 value1, V2 value2);

	/**
	 * Executes the specified caller with a given context.
	 * @param <T> the return type
	 * @param caller a callable task
	 * @return the result of the caller
	 * @throws Exception if execution fails
	 */
	default <T> T execute(Callable<T> caller) throws Exception {
		try {
			return this.execute(org.wildfly.clustering.function.Supplier.call(caller, org.wildfly.clustering.function.Consumer.<Exception>throwing(CompletionException::new).thenReturn(org.wildfly.clustering.function.Supplier.<T>empty())));
		} catch (CompletionException e) {
			throw (Exception) e.getCause();
		}
	}

	/**
	 * Executes the specified supplier with a given context.
	 * @param <T> the return type
	 * @param supplier a supplier task
	 * @return the result of the supplier
	 */
	<T> T execute(Supplier<T> supplier);

	/**
	 * Executes the specified supplier with a given context.
	 * @param <V> the function parameter type
	 * @param <R> the function return type
	 * @param function a function to apply
	 * @param value the function parameter
	 * @return the result of the function
	 */
	<V, R> R execute(Function<V, R> function, V value);

	/**
	 * Executes the specified supplier with a given context.
	 * @param <V1> the 1st function parameter type
	 * @param <V2> the 2nd function parameter type
	 * @param <R> the function return type
	 * @param function a function to apply
	 * @param value1 the 1st function parameter
	 * @param value2 the 2nd function parameter
	 * @return the result of the function
	 */
	<V1, V2, R> R execute(BiFunction<V1, V2, R> function, V1 value1, V2 value2);

	/**
	 * Creates a contextual executor from the specified context provider.
	 * @param <C> the context type
	 * @param provider a supplier of a context
	 * @return a contextual executor using the specified context provider.
	 */
	static <C> ContextualExecutor withContextProvider(Supplier<Context<C>> provider) {
		return new ContextualExecutor() {
			@Override
			public void execute(Runnable runner) {
				try (Context<C> context = provider.get()) {
					runner.run();
				}
			}

			@Override
			public <V> void execute(Consumer<V> consumer, V value) {
				try (Context<C> context = provider.get()) {
					consumer.accept(value);
				}
			}

			@Override
			public <V1, V2> void execute(BiConsumer<V1, V2> consumer, V1 value1, V2 value2) {
				try (Context<C> context = provider.get()) {
					consumer.accept(value1, value2);
				}
			}

			@Override
			public <T> T execute(Supplier<T> supplier) {
				try (Context<C> context = provider.get()) {
					return supplier.get();
				}
			}

			@Override
			public <V, R> R execute(Function<V, R> function, V value) {
				try (Context<C> context = provider.get()) {
					return function.apply(value);
				}
			}

			@Override
			public <V1, V2, R> R execute(BiFunction<V1, V2, R> function, V1 value1, V2 value2) {
				try (Context<C> context = provider.get()) {
					return function.apply(value1, value2);
				}
			}
		};
	}
}
