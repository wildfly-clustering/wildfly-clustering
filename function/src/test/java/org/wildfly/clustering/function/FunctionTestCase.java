/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Unit test for {@link Function}.
 * @author Paul Ferraro
 */
public class FunctionTestCase {

	@Test
	public void empty() {
		Assertions.assertThat(Function.empty().apply(new Object())).isNull();
	}

	@Test
	public void of() {
		Object expected = new Object();
		Assertions.assertThat(Function.of(expected).apply(new Object())).isSameAs(expected);
		Assertions.assertThat(Function.of(null).apply(new Object())).isNull();
	}

	@Test
	public void get() {
		Object expected = new Object();
		Supplier<Object> supplier = Mockito.mock(Supplier.class);
		Mockito.doReturn(expected).when(supplier).get();
		Assertions.assertThat(Function.get(supplier).apply(new Object())).isSameAs(expected);
		Assertions.assertThat(Function.get(null).apply(new Object())).isNull();
	}

	@Test
	public void withDefault() {
		Object result = new Object();
		Object value = new Object();
		Object defaultValue = new Object();
		Object defaultResult = new Object();
		Function<Object, Object> function = Mockito.mock(Function.class);
		Mockito.doCallRealMethod().when(function).withDefault(ArgumentMatchers.any(), ArgumentMatchers.any());
		Predicate<Object> predicate = Mockito.mock(Predicate.class);
		Supplier<Object> supplier = Mockito.mock(Supplier.class);

		Mockito.doReturn(false, true).when(predicate).test(value);
		Mockito.doReturn(defaultValue).when(supplier).get();
		Mockito.doReturn(result).when(function).apply(value);
		Mockito.doReturn(defaultResult).when(function).apply(defaultValue);

		Assertions.assertThat(function.withDefault(predicate, supplier).apply(value)).isSameAs(defaultResult);
		Assertions.assertThat(function.withDefault(predicate, supplier).apply(value)).isSameAs(result);
	}

	@Test
	public void orDefault() {
		Object result = new Object();
		Object value = new Object();
		Object defaultResult = new Object();
		Function<Object, Object> function = Mockito.mock(Function.class);
		Mockito.doCallRealMethod().when(function).orDefault(ArgumentMatchers.any(), ArgumentMatchers.any());
		Predicate<Object> predicate = Mockito.mock(Predicate.class);
		Supplier<Object> supplier = Mockito.mock(Supplier.class);

		Mockito.doReturn(false, true).when(predicate).test(value);
		Mockito.doReturn(defaultResult).when(supplier).get();
		Mockito.doReturn(result).when(function).apply(value);

		Assertions.assertThat(function.orDefault(predicate, supplier).apply(value)).isSameAs(defaultResult);
		Assertions.assertThat(function.orDefault(predicate, supplier).apply(value)).isSameAs(result);
	}
}
