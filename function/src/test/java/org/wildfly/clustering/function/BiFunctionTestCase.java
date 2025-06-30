/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BiFunction}.
 * @author Paul Ferraro
 */
public class BiFunctionTestCase {
	private Object value1 = new Object();
	private Object value2 = new Object();
	private Object result = new Object();

	@Test
	public void empty() {
		BiFunction<Object, Object, Object> function = BiFunction.empty();
		Object result = function.apply(this.value1, this.value2);

		assertThat(result).isNull();
	}

	@Test
	public void of() {
		BiFunction<Object, Object, Object> function = BiFunction.of(this.result);
		Object result = function.apply(this.value1, this.value2);

		assertThat(result).isSameAs(this.result);

		function = BiFunction.of(null);
		result = function.apply(this.value1, this.value2);

		assertThat(result).isNull();
	}

	@Test
	public void get() {
		Supplier<Object> supplier = mock(Supplier.class);
		doReturn(this.result).when(supplier).get();

		BiFunction<Object, Object, Object> function = BiFunction.get(supplier);
		Object result = function.apply(this.value1, this.value2);

		assertThat(result).isSameAs(this.result);

		function = BiFunction.get(null);
		result = function.apply(this.value1, this.value2);

		assertThat(result).isNull();
	}

	@Test
	public void formerValue() {
		BiFunction<Object, Object, Object> function = BiFunction.former();
		Object result = function.apply(this.value1, this.value2);

		assertThat(result).isSameAs(this.value1);
	}

	@Test
	public void latterValue() {
		BiFunction<Object, Object, Object> function = BiFunction.latter();
		Object result = function.apply(this.value1, this.value2);

		assertThat(result).isSameAs(this.value2);
	}

	@Test
	public void formerFunction() {
		Function<Object, Object> function1 = mock(Function.class);
		doReturn(this.result).when(function1).apply(this.value1);

		BiFunction<Object, Object, Object> function = BiFunction.former(function1);
		Object result = function.apply(this.value1, this.value2);

		assertThat(result).isSameAs(this.result);
	}

	@Test
	public void latterFunction() {
		Function<Object, Object> function2 = mock(Function.class);
		doReturn(this.result).when(function2).apply(this.value2);

		BiFunction<Object, Object, Object> function = BiFunction.latter(function2);
		Object result = function.apply(this.value1, this.value2);

		assertThat(result).isSameAs(this.result);
	}

	@Test
	public void andThen() {
		Object interrimResult = new Object();
		BiFunction<Object, Object, Object> before = mock(BiFunction.class);
		Function<Object, Object> after = mock(Function.class);
		doCallRealMethod().when(before).andThen(any());
		doReturn(interrimResult).when(before).apply(this.value1, this.value2);
		doReturn(this.result).when(after).apply(interrimResult);

		Object result = before.andThen(after).apply(this.value1, this.value2);

		assertThat(result).isSameAs(this.result);
	}

	@Test
	public void compose() {
		Object interrimResult1 = new Object();
		Object interrimResult2 = new Object();
		BiFunction<Object, Object, Object> after = mock(BiFunction.class);
		Function<Object, Object> before1 = mock(Function.class);
		Function<Object, Object> before2 = mock(Function.class);
		doCallRealMethod().when(after).compose(any(), any());
		doReturn(interrimResult1).when(before1).apply(this.value1);
		doReturn(interrimResult2).when(before2).apply(this.value2);
		doReturn(this.result).when(after).apply(interrimResult1, interrimResult2);

		Object result = after.compose(before1, before2).apply(this.value1, this.value2);

		assertThat(result).isSameAs(this.result);
	}

	@Test
	public void reverse() {
		BiFunction<Object, Object, Object> function = mock(BiFunction.class);
		doReturn(this.result).when(function).apply(this.value1, this.value2);
		doCallRealMethod().when(function).reverse();

		Object result = function.reverse().apply(this.value2, this.value1);

		assertThat(result).isSameAs(this.result);
	}

	@Test
	public void withDefault() {
		BiFunction<Object, Object, Object> function = mock(BiFunction.class);
		doCallRealMethod().when(function).withDefault(any(), any(), any(), any());
		Predicate<Object> predicate1 = mock(Predicate.class);
		Predicate<Object> predicate2 = mock(Predicate.class);
		Supplier<Object> supplier1 = mock(Supplier.class);
		Supplier<Object> supplier2 = mock(Supplier.class);
		Object expectedWhenDefaultValue1 = new Object();
		Object expectedWhenDefaultValue2 = new Object();
		Object expectedWhenDefaultValues = new Object();
		Object defaultValue1 = new Object();
		Object defaultValue2 = new Object();

		doReturn(false, false, true, true).when(predicate1).test(this.value1);
		doReturn(false, true, false, true).when(predicate2).test(this.value2);
		doReturn(defaultValue1).when(supplier1).get();
		doReturn(defaultValue2).when(supplier2).get();
		doReturn(this.result).when(function).apply(this.value1, this.value2);
		doReturn(expectedWhenDefaultValues).when(function).apply(defaultValue1, defaultValue2);
		doReturn(expectedWhenDefaultValue1).when(function).apply(defaultValue1, this.value2);
		doReturn(expectedWhenDefaultValue2).when(function).apply(this.value1, defaultValue2);

		assertThat(function.withDefault(predicate1, supplier1, predicate2, supplier2).apply(this.value1, this.value2)).isSameAs(expectedWhenDefaultValues);
		assertThat(function.withDefault(predicate1, supplier1, predicate2, supplier2).apply(this.value1, this.value2)).isSameAs(expectedWhenDefaultValue1);
		assertThat(function.withDefault(predicate1, supplier1, predicate2, supplier2).apply(this.value1, this.value2)).isSameAs(expectedWhenDefaultValue2);
		assertThat(function.withDefault(predicate1, supplier1, predicate2, supplier2).apply(this.value1, this.value2)).isSameAs(this.result);
	}

	@Test
	public void orDefault() {
		BiFunction<Object, Object, Object> function = mock(BiFunction.class);
		doCallRealMethod().when(function).orDefault(any(), any());
		BiPredicate<Object, Object> predicate = mock(BiPredicate.class);
		Supplier<Object> supplier = mock(Supplier.class);
		Object defaultValue = new Object();

		doReturn(false, true).when(predicate).test(this.value1, this.value2);
		doReturn(defaultValue).when(supplier).get();
		doReturn(this.result).when(function).apply(this.value1, this.value2);

		assertThat(function.orDefault(predicate, supplier).apply(this.value1, this.value2)).isSameAs(defaultValue);
		assertThat(function.orDefault(predicate, supplier).apply(this.value1, this.value2)).isSameAs(this.result);
	}
}
