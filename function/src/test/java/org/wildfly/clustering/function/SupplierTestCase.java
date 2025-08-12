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
import org.mockito.ArgumentMatchers;

/**
 * Unit test for {@link Supplier}.
 * @author Paul Ferraro
 */
public class SupplierTestCase {
	private final Random random = new Random();

	@Test
	public void map() {
		Object value = new Object();
		Object expected = new Object();
		boolean expectedBoolean = this.random.nextBoolean();
		double expectedDouble = this.random.nextDouble();
		int expectedInt = this.random.nextInt();
		long expectedLong = this.random.nextLong();
		Supplier<Object> supplier = mock(Supplier.class);
		doCallRealMethod().when(supplier).map(ArgumentMatchers.<Function<Object, Object>>any());
		doCallRealMethod().when(supplier).mapAsBoolean(ArgumentMatchers.<Predicate<Object>>any());
		doCallRealMethod().when(supplier).mapAsDouble(ArgumentMatchers.<ToDoubleFunction<Object>>any());
		doCallRealMethod().when(supplier).mapAsInt(ArgumentMatchers.<ToIntFunction<Object>>any());
		doCallRealMethod().when(supplier).mapAsLong(ArgumentMatchers.<ToLongFunction<Object>>any());
		doReturn(value).when(supplier).get();
		Function<Object, Object> mapper = mock(Function.class);
		Predicate<Object> predicate = mock(Predicate.class);
		ToDoubleFunction<Object> doubleMapper = mock(ToDoubleFunction.class);
		ToIntFunction<Object> intMapper = mock(ToIntFunction.class);
		ToLongFunction<Object> longMapper = mock(ToLongFunction.class);
		doReturn(expected).when(mapper).apply(value);
		doReturn(expectedBoolean).when(predicate).test(value);
		doReturn(expectedDouble).when(doubleMapper).applyAsDouble(value);
		doReturn(expectedInt).when(intMapper).applyAsInt(value);
		doReturn(expectedLong).when(longMapper).applyAsLong(value);

		assertThat(supplier.map(mapper).get()).isSameAs(expected);
		assertThat(supplier.mapAsBoolean(predicate).getAsBoolean()).isEqualTo(expectedBoolean);
		assertThat(supplier.mapAsDouble(doubleMapper).getAsDouble()).isEqualTo(expectedDouble);
		assertThat(supplier.mapAsInt(intMapper).getAsInt()).isEqualTo(expectedInt);
		assertThat(supplier.mapAsLong(longMapper).getAsLong()).isEqualTo(expectedLong);
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
