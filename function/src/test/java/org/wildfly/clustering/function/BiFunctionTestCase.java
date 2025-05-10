/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Unit test for {@link BiFunction}.
 * @author Paul Ferraro
 */
public class BiFunctionTestCase {
	private Object value1 = new Object();
	private Object value2 = new Object();
	private Object result = new Object();

	@Test
	public void ofValue() {
		BiFunction<Object, Object, Object> function = BiFunction.of(this.result);
		Object result = function.apply(this.value1, this.value2);

		Assertions.assertThat(result).isSameAs(this.result);

		function = BiFunction.of(null);
		result = function.apply(this.value1, this.value2);

		Assertions.assertThat(result).isNull();
	}

	@Test
	public void ofSupplier() {
		Supplier<Object> supplier = Mockito.mock(Supplier.class);
		Mockito.doReturn(this.result).when(supplier).get();

		BiFunction<Object, Object, Object> function = BiFunction.of(supplier);
		Object result = function.apply(this.value1, this.value2);

		Assertions.assertThat(result).isSameAs(this.result);

		supplier = null;
		function = BiFunction.of(supplier);
		result = function.apply(this.value1, this.value2);

		Assertions.assertThat(result).isNull();
	}

	@Test
	public void formerValue() {
		BiFunction<Object, Object, Object> function = BiFunction.former();
		Object result = function.apply(this.value1, this.value2);

		Assertions.assertThat(result).isSameAs(this.value1);
	}

	@Test
	public void latterValue() {
		BiFunction<Object, Object, Object> function = BiFunction.latter();
		Object result = function.apply(this.value1, this.value2);

		Assertions.assertThat(result).isSameAs(this.value2);
	}

	@Test
	public void formerFunction() {
		Function<Object, Object> function1 = Mockito.mock(Function.class);
		Mockito.doReturn(this.result).when(function1).apply(this.value1);

		BiFunction<Object, Object, Object> function = BiFunction.former(function1);
		Object result = function.apply(this.value1, this.value2);

		Assertions.assertThat(result).isSameAs(this.result);
	}

	@Test
	public void latterFunction() {
		Function<Object, Object> function2 = Mockito.mock(Function.class);
		Mockito.doReturn(this.result).when(function2).apply(this.value2);

		BiFunction<Object, Object, Object> function = BiFunction.latter(function2);
		Object result = function.apply(this.value1, this.value2);

		Assertions.assertThat(result).isSameAs(this.result);
	}

	@Test
	public void andThen() {
		Object interrimResult = new Object();
		BiFunction<Object, Object, Object> before = Mockito.mock(BiFunction.class);
		Function<Object, Object> after = Mockito.mock(Function.class);
		Mockito.doCallRealMethod().when(before).andThen(ArgumentMatchers.any());
		Mockito.doReturn(interrimResult).when(before).apply(this.value1, this.value2);
		Mockito.doReturn(this.result).when(after).apply(interrimResult);

		Object result = before.andThen(after).apply(this.value1, this.value2);

		Assertions.assertThat(result).isSameAs(this.result);
	}

	@Test
	public void compose() {
		Object interrimResult1 = new Object();
		Object interrimResult2 = new Object();
		BiFunction<Object, Object, Object> after = Mockito.mock(BiFunction.class);
		Function<Object, Object> before1 = Mockito.mock(Function.class);
		Function<Object, Object> before2 = Mockito.mock(Function.class);
		Mockito.doCallRealMethod().when(after).compose(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.doReturn(interrimResult1).when(before1).apply(this.value1);
		Mockito.doReturn(interrimResult2).when(before2).apply(this.value2);
		Mockito.doReturn(this.result).when(after).apply(interrimResult1, interrimResult2);

		Object result = after.compose(before1, before2).apply(this.value1, this.value2);

		Assertions.assertThat(result).isSameAs(this.result);
	}

	@Test
	public void reverse() {
		BiFunction<Object, Object, Object> function = Mockito.mock(BiFunction.class);
		Mockito.doReturn(this.result).when(function).apply(this.value1, this.value2);
		Mockito.doCallRealMethod().when(function).reverse();

		Object result = function.reverse().apply(this.value2, this.value1);

		Assertions.assertThat(result).isSameAs(this.result);
	}

	@Test
	public void withDefault() {
		BiFunction<Object, Object, Object> function = Mockito.mock(BiFunction.class);
		Mockito.doCallRealMethod().when(function).withDefault(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
		Predicate<Object> predicate1 = Mockito.mock(Predicate.class);
		Predicate<Object> predicate2 = Mockito.mock(Predicate.class);
		Supplier<Object> supplier1 = Mockito.mock(Supplier.class);
		Supplier<Object> supplier2 = Mockito.mock(Supplier.class);
		Object expectedWhenDefaultValue1 = new Object();
		Object expectedWhenDefaultValue2 = new Object();
		Object expectedWhenDefaultValues = new Object();
		Object defaultValue1 = new Object();
		Object defaultValue2 = new Object();

		Mockito.doReturn(false, false, true, true).when(predicate1).test(this.value1);
		Mockito.doReturn(false, true, false, true).when(predicate2).test(this.value2);
		Mockito.doReturn(defaultValue1).when(supplier1).get();
		Mockito.doReturn(defaultValue2).when(supplier2).get();
		Mockito.doReturn(this.result).when(function).apply(this.value1, this.value2);
		Mockito.doReturn(expectedWhenDefaultValues).when(function).apply(defaultValue1, defaultValue2);
		Mockito.doReturn(expectedWhenDefaultValue1).when(function).apply(defaultValue1, this.value2);
		Mockito.doReturn(expectedWhenDefaultValue2).when(function).apply(this.value1, defaultValue2);

		Assertions.assertThat(function.withDefault(predicate1, supplier1, predicate2, supplier2).apply(this.value1, this.value2)).isSameAs(expectedWhenDefaultValues);
		Assertions.assertThat(function.withDefault(predicate1, supplier1, predicate2, supplier2).apply(this.value1, this.value2)).isSameAs(expectedWhenDefaultValue1);
		Assertions.assertThat(function.withDefault(predicate1, supplier1, predicate2, supplier2).apply(this.value1, this.value2)).isSameAs(expectedWhenDefaultValue2);
		Assertions.assertThat(function.withDefault(predicate1, supplier1, predicate2, supplier2).apply(this.value1, this.value2)).isSameAs(this.result);
	}

	@Test
	public void orDefault() {
		BiFunction<Object, Object, Object> function = Mockito.mock(BiFunction.class);
		Mockito.doCallRealMethod().when(function).orDefault(ArgumentMatchers.any(), ArgumentMatchers.any());
		BiPredicate<Object, Object> predicate = Mockito.mock(BiPredicate.class);
		Supplier<Object> supplier = Mockito.mock(Supplier.class);
		Object defaultValue = new Object();

		Mockito.doReturn(false, true).when(predicate).test(this.value1, this.value2);
		Mockito.doReturn(defaultValue).when(supplier).get();
		Mockito.doReturn(this.result).when(function).apply(this.value1, this.value2);

		Assertions.assertThat(function.orDefault(predicate, supplier).apply(this.value1, this.value2)).isSameAs(defaultValue);
		Assertions.assertThat(function.orDefault(predicate, supplier).apply(this.value1, this.value2)).isSameAs(this.result);
	}
}
