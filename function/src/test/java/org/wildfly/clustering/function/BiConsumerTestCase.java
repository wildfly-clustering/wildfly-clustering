/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Unit test for {@link BiConsumer}.
 * @author Paul Ferraro
 */
public class BiConsumerTestCase {
	private final Object value1 = new Object();
	private final Object value2 = new Object();
	private final Random random = new Random();

	@Test
	public void empty() {
		BiConsumer<Object, Object> consumer = BiConsumer.of();
		consumer.accept(this.value1, this.value2);
		consumer.accept(this.value1, null);
		consumer.accept(null, this.value2);
		consumer.accept(null, null);
	}

	@Test
	public void thenReturn() {
		Object expected = new Object();
		BiConsumer<Object, Object> consumer = mock(BiConsumer.class);
		Supplier<Object> supplier = mock(Supplier.class);
		doReturn(expected).when(supplier).get();

		doCallRealMethod().when(consumer).thenReturn(any());

		assertThat(consumer.thenReturn(supplier).apply(this.value1, this.value2)).isSameAs(expected);

		verify(consumer).accept(this.value1, this.value2);
	}

	@Test
	public void thenReturnBoolean() {
		boolean expected = this.random.nextBoolean();
		BiConsumer<Object, Object> consumer = mock(BiConsumer.class);
		BooleanSupplier supplier = mock(BooleanSupplier.class);
		doReturn(expected).when(supplier).getAsBoolean();

		doCallRealMethod().when(consumer).thenReturnBoolean(any());

		assertThat(consumer.thenReturnBoolean(supplier).test(this.value1, this.value2)).isEqualTo(expected);

		verify(consumer).accept(this.value1, this.value2);
	}

	@Test
	public void thenReturnDouble() {
		double expected = this.random.nextDouble();
		BiConsumer<Object, Object> consumer = mock(BiConsumer.class);
		DoubleSupplier supplier = mock(DoubleSupplier.class);
		doReturn(expected).when(supplier).getAsDouble();

		doCallRealMethod().when(consumer).thenReturnDouble(any());

		assertThat(consumer.thenReturnDouble(supplier).applyAsDouble(this.value1, this.value2)).isEqualTo(expected);

		verify(consumer).accept(this.value1, this.value2);
	}

	@Test
	public void thenReturnInt() {
		int expected = this.random.nextInt();
		BiConsumer<Object, Object> consumer = mock(BiConsumer.class);
		IntSupplier supplier = mock(IntSupplier.class);
		doReturn(expected).when(supplier).getAsInt();

		doCallRealMethod().when(consumer).thenReturnInt(any());

		assertThat(consumer.thenReturnInt(supplier).applyAsInt(this.value1, this.value2)).isEqualTo(expected);

		verify(consumer).accept(this.value1, this.value2);
	}

	@Test
	public void thenReturnLong() {
		long expected = this.random.nextLong();
		BiConsumer<Object, Object> consumer = mock(BiConsumer.class);
		LongSupplier supplier = mock(LongSupplier.class);
		doReturn(expected).when(supplier).getAsLong();

		doCallRealMethod().when(consumer).thenReturnLong(any());

		assertThat(consumer.thenReturnLong(supplier).applyAsLong(this.value1, this.value2)).isEqualTo(expected);

		verify(consumer).accept(this.value1, this.value2);
	}

	@Test
	public void compose() {
		BiConsumer<Object, Object> consumer = Mockito.mock(BiConsumer.class);
		Mockito.doCallRealMethod().when(consumer).compose(any(), any());
		Function<Object, Object> mapper1 = Mockito.mock(Function.class);
		Function<Object, Object> mapper2 = Mockito.mock(Function.class);
		Object value1 = new Object();
		Object value2 = new Object();
		Object mapped1 = new Object();
		Object mapped2 = new Object();
		Mockito.doReturn(mapped1).when(mapper1).apply(value1);
		Mockito.doReturn(mapped2).when(mapper2).apply(value2);

		consumer.compose(mapper1, mapper2).accept(value1, value2);

		Mockito.verify(consumer).accept(mapped1, mapped2);
	}

	@Test
	public void composeUnary() {
		BiConsumer<Object, Object> consumer = Mockito.mock(BiConsumer.class);
		Mockito.doCallRealMethod().when(consumer).composeUnary(any(), any());
		Function<Object, Object> mapper1 = Mockito.mock(Function.class);
		Function<Object, Object> mapper2 = Mockito.mock(Function.class);
		Object value = new Object();
		Object mapped1 = new Object();
		Object mapped2 = new Object();
		Mockito.doReturn(mapped1).when(mapper1).apply(value);
		Mockito.doReturn(mapped2).when(mapper2).apply(value);

		consumer.composeUnary(mapper1, mapper2).accept(value);

		Mockito.verify(consumer).accept(mapped1, mapped2);
	}

	@Test
	public void andThen() {
		BiConsumer<Object, Object> before = Mockito.mock(BiConsumer.class);
		BiConsumer<Object, Object> after = Mockito.mock(BiConsumer.class);
		Mockito.doCallRealMethod().when(before).andThen(any());
		InOrder order = Mockito.inOrder(before, after);

		before.andThen(after).accept(this.value1, this.value2);

		order.verify(before).accept(this.value1, this.value2);
		order.verify(after).accept(this.value1, this.value2);
	}

	@Test
	public void reverse() {
		BiConsumer<Object, Object> consumer = Mockito.mock(BiConsumer.class);
		Mockito.doCallRealMethod().when(consumer).reverse();

		consumer.reverse().accept(this.value1, this.value2);

		Mockito.verify(consumer).accept(this.value2, this.value1);
	}

	@Test
	public void composite() {
		BiConsumer<Object, Object> consumer1 = Mockito.mock(BiConsumer.class);
		BiConsumer<Object, Object> consumer2 = Mockito.mock(BiConsumer.class);
		BiConsumer<Object, Object> consumer3 = Mockito.mock(BiConsumer.class);
		InOrder order = Mockito.inOrder(consumer1, consumer2, consumer3);

		BiConsumer.of(List.of(consumer1, consumer2, consumer3)).accept(this.value1, this.value2);

		order.verify(consumer1).accept(this.value1, this.value2);
		order.verify(consumer2).accept(this.value1, this.value2);
		order.verify(consumer3).accept(this.value1, this.value2);
	}

	@Test
	public void of() {
		Consumer<Object> former = Mockito.mock(Consumer.class);
		Consumer<Object> latter = Mockito.mock(Consumer.class);
		InOrder order = Mockito.inOrder(former, latter);

		BiConsumer.of(former, latter).accept(this.value1, this.value2);

		order.verify(former).accept(this.value1);
		order.verify(latter).accept(this.value2);
	}
}
