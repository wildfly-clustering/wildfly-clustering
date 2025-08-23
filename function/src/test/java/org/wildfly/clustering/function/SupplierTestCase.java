/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Random;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link Supplier}.
 * @author Paul Ferraro
 */
public class SupplierTestCase {
	private final Random random = new Random();

	@Test
	public void apply() {
		Object value = new Object();
		Object expected = new Object();
		boolean expectedBoolean = this.random.nextBoolean();
		double expectedDouble = this.random.nextDouble();
		int expectedInt = this.random.nextInt();
		long expectedLong = this.random.nextLong();
		Supplier<Object> supplier = mock(Supplier.class);
		doCallRealMethod().when(supplier).thenApply(any());
		doCallRealMethod().when(supplier).thenAccept(any());
		doCallRealMethod().when(supplier).thenTest(any());
		doCallRealMethod().when(supplier).thenApplyAsDouble(any());
		doCallRealMethod().when(supplier).thenApplyAsInt(any());
		doCallRealMethod().when(supplier).thenApplyAsLong(any());
		doReturn(value).when(supplier).get();

		Function<Object, Object> function = mock(Function.class);
		Predicate<Object> predicate = mock(Predicate.class);
		ToDoubleFunction<Object> doubleMapper = mock(ToDoubleFunction.class);
		ToIntFunction<Object> intMapper = mock(ToIntFunction.class);
		ToLongFunction<Object> longMapper = mock(ToLongFunction.class);
		Consumer<Object> unaryConsumer = mock(Consumer.class);

		doReturn(expected).when(function).apply(value);
		doReturn(expectedBoolean).when(predicate).test(value);
		doReturn(expectedDouble).when(doubleMapper).applyAsDouble(value);
		doReturn(expectedInt).when(intMapper).applyAsInt(value);
		doReturn(expectedLong).when(longMapper).applyAsLong(value);

		assertThat(supplier.thenApply(function).get()).isSameAs(expected);
		assertThat(supplier.thenTest(predicate).getAsBoolean()).isEqualTo(expectedBoolean);
		assertThat(supplier.thenApplyAsDouble(doubleMapper).getAsDouble()).isEqualTo(expectedDouble);
		assertThat(supplier.thenApplyAsInt(intMapper).getAsInt()).isEqualTo(expectedInt);
		assertThat(supplier.thenApplyAsLong(longMapper).getAsLong()).isEqualTo(expectedLong);

		supplier.thenAccept(unaryConsumer).run();

		verify(unaryConsumer).accept(value);
	}

	@Test
	public void handle() {
		Supplier<Object> supplier = mock(Supplier.class);
		Function<RuntimeException, Object> handler = mock(Function.class);
		doCallRealMethod().when(supplier).handle(any());
		Object value = new Object();
		Object handled = new Object();
		RuntimeException exception = new RuntimeException();

		doReturn(value).when(supplier).get();

		assertThat(supplier.handle(handler).get()).isSameAs(value);

		verify(supplier).get();
		verify(handler, never()).apply(any());

		doThrow(exception).when(supplier).get();
		doReturn(handled).when(handler).apply(exception);

		assertThat(supplier.handle(handler).get()).isSameAs(handled);

		verify(supplier, times(2)).get();
		verify(handler).apply(any());
	}

	@Test
	public void of() {
		Object expected = new Object();
		assertThat(Supplier.of(expected).get()).isSameAs(expected);
		assertThat(Supplier.of(null).get()).isNull();
	}

	@Test
	public void run() {
		Runnable runner = mock(Runnable.class);
		assertThat(Supplier.run(runner).get()).isNull();
		verify(runner).run();
		assertThat(Supplier.run(null).get()).isNull();
	}

	@Test
	public void call() throws Exception {
		Callable<Object> caller = mock(Callable.class);
		Function<Exception, Object> handler = mock(Function.class);

		assertThat(Supplier.call(caller, handler).get()).isNull();

		verify(caller).call();
		verify(handler, never()).apply(any());

		Object result = new Object();

		doReturn(result).when(caller).call();

		assertThat(Supplier.call(caller, handler).get()).isSameAs(result);

		verify(caller, times(2)).call();
		verify(handler, never()).apply(any());

		Exception exception = new Exception();
		Object handled = new Object();

		doThrow(exception).when(caller).call();
		doReturn(handled).when(handler).apply(exception);

		assertThat(Supplier.call(caller, handler).get()).isSameAs(handled);

		verify(caller, times(3)).call();
		verify(handler).apply(exception);
	}
}
