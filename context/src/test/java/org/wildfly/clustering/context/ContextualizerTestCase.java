/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.context;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.wildfly.common.function.ExceptionRunnable;
import org.wildfly.common.function.ExceptionSupplier;

/**
 * Unit test for {@link Contextualizer}.
 * @author Paul Ferraro
 */
public class ContextualizerTestCase {

	@Test
	public void test() throws Exception {
		Object original = new Object();
		Object target = new Object();
		Object result = new Object();
		AtomicReference<Object> resultRef = new AtomicReference<>();
		ContextReference<Object> contextRef = new AtomicContextReference<>(original);
		Contextualizer contextualizer = Contextualizer.withContextProvider(contextRef.provide(target));

		Runnable runner = new Runnable() {
			@Override
			public void run() {
				assertThat(contextRef.get()).isSameAs(target);
				resultRef.set(result);
			}
		};

		assertThat(contextRef.get()).isSameAs(original);
		contextualizer.contextualize(runner).run();
		assertThat(contextRef.get()).isSameAs(original);

		assertThat(resultRef.get()).isSameAs(result);
		resultRef.set(null);

		ExceptionRunnable<Exception> exceptionRunner = new ExceptionRunnable<>() {
			@Override
			public void run() throws Exception {
				assertThat(contextRef.get()).isSameAs(target);
				resultRef.set(result);
			}
		};

		assertThat(contextRef.get()).isSameAs(original);
		contextualizer.contextualize(exceptionRunner).run();
		assertThat(contextRef.get()).isSameAs(original);

		assertThat(resultRef.get()).isSameAs(result);
		resultRef.set(null);

		Callable<Object> caller = new Callable<>() {
			@Override
			public Object call() {
				assertThat(contextRef.get()).isSameAs(target);
				return result;
			}
		};

		assertThat(contextRef.get()).isSameAs(original);
		assertThat(contextualizer.contextualize(caller).call()).isSameAs(result);
		assertThat(contextRef.get()).isSameAs(original);

		Supplier<Object> supplier = new Supplier<>() {
			@Override
			public Object get() {
				assertThat(contextRef.get()).isSameAs(target);
				return result;
			}
		};

		assertThat(contextRef.get()).isSameAs(original);
		assertThat(contextualizer.contextualize(supplier).get()).isSameAs(result);
		assertThat(contextRef.get()).isSameAs(original);

		ExceptionSupplier<Object, Exception> exceptionSupplier = new ExceptionSupplier<>() {
			@Override
			public Object get() {
				assertThat(contextRef.get()).isSameAs(target);
				return result;
			}
		};

		assertThat(contextRef.get()).isSameAs(original);
		assertThat(contextualizer.contextualize(exceptionSupplier).get()).isSameAs(result);
		assertThat(contextRef.get()).isSameAs(original);
	}

	static class AtomicContextReference<T> implements ContextReference<T> {
		private AtomicReference<T> ref;

		AtomicContextReference(T initial) {
			this.ref = new AtomicReference<>(initial);
		}

		@Override
		public void accept(T value) {
			this.ref.set(value);
		}

		@Override
		public T get() {
			return this.ref.get();
		}
	}
}
