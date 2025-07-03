/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BiFunction}.
 * @author Paul Ferraro
 */
public class BinaryOperatorTestCase {
	private Object value1 = new Object();
	private Object value2 = new Object();
	private Object result = new Object();

	@Test
	public void test() {
		assertThat(BinaryOperator.empty().apply(this.value1, this.value2)).isNull();
		assertThat(BinaryOperator.of(this.result).apply(this.value1, this.value2)).isSameAs(this.result);
		assertThat(BinaryOperator.of(null).apply(this.value1, this.value2)).isNull();
		assertThat(BinaryOperator.former().apply(this.value1, this.value2)).isSameAs(this.value1);
		assertThat(BinaryOperator.latter().apply(this.value1, this.value2)).isSameAs(this.value2);
		assertThat(BinaryOperator.get(() -> this.result).apply(this.value1, this.value2)).isSameAs(this.result);
		assertThat(BinaryOperator.get(() -> null).apply(this.value1, this.value2)).isNull();
	}

	@Test
	public void andThen() {
		Object interrimResult = new Object();
		BinaryOperator<Object> before = mock(BinaryOperator.class);
		UnaryOperator<Object> after = mock(UnaryOperator.class);
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
		BinaryOperator<Object> after = mock(BinaryOperator.class);
		UnaryOperator<Object> before1 = mock(UnaryOperator.class);
		UnaryOperator<Object> before2 = mock(UnaryOperator.class);
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
