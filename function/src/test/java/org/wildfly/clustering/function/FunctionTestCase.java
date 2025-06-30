/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link Function}.
 * @author Paul Ferraro
 */
public class FunctionTestCase {

	@Test
	public void empty() {
		assertThat(Function.empty().apply(new Object())).isNull();
	}

	@Test
	public void of() {
		Object expected = new Object();
		assertThat(Function.of(expected).apply(new Object())).isSameAs(expected);
		assertThat(Function.of(null).apply(new Object())).isNull();
	}

	@Test
	public void get() {
		Object expected = new Object();
		Supplier<Object> supplier = mock(Supplier.class);
		doReturn(expected).when(supplier).get();
		assertThat(Function.get(supplier).apply(new Object())).isSameAs(expected);
		assertThat(Function.get(null).apply(new Object())).isNull();
	}

	@Test
	public void withDefault() {
		Object result = new Object();
		Object value = new Object();
		Object defaultValue = new Object();
		Object defaultResult = new Object();
		Function<Object, Object> function = mock(Function.class);
		doCallRealMethod().when(function).withDefault(any(), any());
		Predicate<Object> predicate = mock(Predicate.class);
		Supplier<Object> supplier = mock(Supplier.class);

		doReturn(false, true).when(predicate).test(value);
		doReturn(defaultValue).when(supplier).get();
		doReturn(result).when(function).apply(value);
		doReturn(defaultResult).when(function).apply(defaultValue);

		assertThat(function.withDefault(predicate, supplier).apply(value)).isSameAs(defaultResult);
		assertThat(function.withDefault(predicate, supplier).apply(value)).isSameAs(result);
	}

	@Test
	public void orDefault() {
		Object result = new Object();
		Object value = new Object();
		Object defaultResult = new Object();
		Function<Object, Object> function = mock(Function.class);
		doCallRealMethod().when(function).orDefault(any(), any());
		Predicate<Object> predicate = mock(Predicate.class);
		Supplier<Object> supplier = mock(Supplier.class);

		doReturn(false, true).when(predicate).test(value);
		doReturn(defaultResult).when(supplier).get();
		doReturn(result).when(function).apply(value);

		assertThat(function.orDefault(predicate, supplier).apply(value)).isSameAs(defaultResult);
		assertThat(function.orDefault(predicate, supplier).apply(value)).isSameAs(result);
	}

	@Test
	public void handle() {
		Function<Object, Object> function = mock(Function.class);
		BiFunction<Object, RuntimeException, Object> handler = mock(BiFunction.class);
		doCallRealMethod().when(function).handle(any());

		Object goodValue = new Object();
		Object badValue = new Object();
		Object result = new Object();
		Object handled = new Object();
		RuntimeException exception = new RuntimeException();

		doReturn(result).when(function).apply(goodValue);
		doThrow(exception).when(function).apply(badValue);
		doReturn(handled).when(handler).apply(badValue, exception);

		assertThat(function.handle(handler).apply(goodValue)).isSameAs(result);
		assertThat(function.handle(handler).apply(badValue)).isSameAs(handled);

		verify(function).apply(goodValue);
		verify(function).apply(badValue);
		verify(handler).apply(badValue, exception);
	}
}
