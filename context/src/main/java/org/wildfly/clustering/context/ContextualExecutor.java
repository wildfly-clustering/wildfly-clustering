/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
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
 * Facility for contextual execution.
 * @author Paul Ferraro
 */
public interface ContextualExecutor extends Executor {

	@Override
	default void execute(Runnable runner) {
		ExceptionRunnable<RuntimeException> r = runner::run;
		this.execute(r);
	}

	/**
	 * Executes the specified runner.
	 * @param <E> the exception type
	 * @param runner a runnable task
	 * @throws E if execution fails
	 */
	<E extends Exception> void execute(ExceptionRunnable<E> runner) throws E;

	/**
	 * Executes the specified consumer using the specified value.
	 * @param <V> the consumed value type
	 * @param consumer a consumer
	 * @param value the consumed value
	 */
	default <V> void execute(Consumer<V> consumer, V value) {
		ExceptionConsumer<V, RuntimeException> c = consumer::accept;
		this.execute(c, value);
	}

	/**
	 * Executes the specified consumer using the specified value.
	 * @param <V> the consumed value type
	 * @param <E> the exception type
	 * @param consumer a consumer
	 * @param value the consumed value
	 * @throws E if execution fails
	 */
	<V, E extends Exception> void execute(ExceptionConsumer<V, E> consumer, V value) throws E;

	/**
	 * Executes the specified runner.
	 * @param <V1> the 1st consumed value type
	 * @param <V2> the 2nd consumed value type
	 * @param consumer a consumer
	 * @param value1 the 1st consumed value
	 * @param value2 the 2nd consumed value
	 */
	default <V1, V2> void execute(BiConsumer<V1, V2> consumer, V1 value1, V2 value2) {
		ExceptionBiConsumer<V1, V2, RuntimeException> c = consumer::accept;
		this.execute(c, value1, value2);
	}

	/**
	 * Executes the specified runner.
	 * @param <T> the 1st consumed value type
	 * @param <V> the 2nd consumed value type
	 * @param <E> the exception type
	 * @param consumer a consumer
	 * @param value1 the 1st consumed value
	 * @param value2 the 2nd consumed value
	 * @throws E if execution fails
	 */
	<T, V, E extends Exception> void execute(ExceptionBiConsumer<T, V, E> consumer, T value1, V value2) throws E;

	/**
	 * Executes the specified caller with a given context.
	 * @param <T> the return type
	 * @param caller a callable task
	 * @return the result of the caller
	 * @throws Exception if execution fails
	 */
	default <T> T execute(Callable<T> caller) throws Exception {
		ExceptionSupplier<T, Exception> supplier = caller::call;
		return this.execute(supplier);
	}

	/**
	 * Executes the specified supplier with a given context.
	 * @param <T> the return type
	 * @param supplier a supplier task
	 * @return the result of the supplier
	 */
	default <T> T execute(Supplier<T> supplier) {
		ExceptionSupplier<T, RuntimeException> s = supplier::get;
		return this.execute(s);
	}

	/**
	 * Executes the specified supplier with a given context.
	 * @param <T> the return type
	 * @param <E> the exception type
	 * @param supplier a supplier task
	 * @return the result of the supplier
	 * @throws E if execution fails
	 */
	<T, E extends Exception> T execute(ExceptionSupplier<T, E> supplier) throws E;

	/**
	 * Executes the specified supplier with a given context.
	 * @param <V> the function parameter type
	 * @param <R> the function return type
	 * @param function a function to apply
	 * @param value the function parameter
	 * @return the result of the function
	 */
	default <V, R> R execute(Function<V, R> function, V value) {
		ExceptionFunction<V, R, RuntimeException> f = function::apply;
		return this.execute(f, value);
	}

	/**
	 * Executes the specified supplier with a given context.
	 * @param <V> the function parameter type
	 * @param <R> the function return type
	 * @param <E> the exception type
	 * @param function a function to apply
	 * @param value the function parameter
	 * @return the result of the function
	 * @throws E if execution fails
	 */
	<V, R, E extends Exception> R execute(ExceptionFunction<V, R, E> function, V value) throws E;

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
	default <V1, V2, R> R execute(BiFunction<V1, V2, R> function, V1 value1, V2 value2) {
		ExceptionBiFunction<V1, V2, R, RuntimeException> f = function::apply;
		return this.execute(f, value1, value2);
	}

	/**
	 * Executes the specified supplier with a given context.
	 * @param <V1> the 1st function parameter type
	 * @param <V2> the 2nd function parameter type
	 * @param <R> the function return type
	 * @param <E> the exception type
	 * @param function a function to apply
	 * @param value1 the 1st function parameter
	 * @param value2 the 2nd function parameter
	 * @return the result of the function
	 * @throws E if execution fails
	 */
	<V1, V2, R, E extends Exception> R execute(ExceptionBiFunction<V1, V2, R, E> function, V1 value1, V2 value2) throws E;

	static ContextualExecutor withContextProvider(Supplier<Context> provider) {
		return new ContextualExecutor() {
			@Override
			public <E extends Exception> void execute(ExceptionRunnable<E> runner) throws E {
				try (Context context = provider.get()) {
					runner.run();
				}
			}

			@Override
			public <V, E extends Exception> void execute(ExceptionConsumer<V, E> consumer, V value) throws E {
				try (Context context = provider.get()) {
					consumer.accept(value);
				}
			}

			@Override
			public <T, V, E extends Exception> void execute(ExceptionBiConsumer<T, V, E> consumer, T value1, V value2) throws E {
				try (Context context = provider.get()) {
					consumer.accept(value1, value2);
				}
			}

			@Override
			public <T, E extends Exception> T execute(ExceptionSupplier<T, E> supplier) throws E {
				try (Context context = provider.get()) {
					return supplier.get();
				}
			}

			@Override
			public <V, R, E extends Exception> R execute(ExceptionFunction<V, R, E> function, V value) throws E {
				try (Context context = provider.get()) {
					return function.apply(value);
				}
			}

			@Override
			public <V1, V2, R, E extends Exception> R execute(ExceptionBiFunction<V1, V2, R, E> function, V1 value1, V2 value2) throws E {
				try (Context context = provider.get()) {
					return function.apply(value1, value2);
				}
			}
		};
	}
}
