/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * Unit test for {@link DoubleConsumer}.
 * @author Paul Ferraro
 */
public class DoubleConsumerTestCase {
	private final Random random = new Random();

	@Test
	public void thenReturn() {
		double value = this.random.nextDouble();
		Object expected = new Object();
		DoubleConsumer consumer = mock(DoubleConsumer.class);
		Supplier<Object> supplier = mock(Supplier.class);
		doReturn(expected).when(supplier).get();

		doCallRealMethod().when(consumer).thenReturn(any());

		assertThat(consumer.thenReturn(supplier).apply(value)).isSameAs(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnBoolean() {
		double value = this.random.nextDouble();
		boolean expected = this.random.nextBoolean();
		DoubleConsumer consumer = mock(DoubleConsumer.class);
		BooleanSupplier supplier = mock(BooleanSupplier.class);
		doReturn(expected).when(supplier).getAsBoolean();

		doCallRealMethod().when(consumer).thenReturnBoolean(any());

		assertThat(consumer.thenReturnBoolean(supplier).test(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnDouble() {
		double value = this.random.nextDouble();
		double expected = this.random.nextDouble();
		DoubleConsumer consumer = mock(DoubleConsumer.class);
		DoubleSupplier supplier = mock(DoubleSupplier.class);
		doReturn(expected).when(supplier).getAsDouble();

		doCallRealMethod().when(consumer).thenReturnDouble(any());

		assertThat(consumer.thenReturnDouble(supplier).applyAsDouble(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnInt() {
		double value = this.random.nextDouble();
		int expected = this.random.nextInt();
		DoubleConsumer consumer = mock(DoubleConsumer.class);
		IntSupplier supplier = mock(IntSupplier.class);
		doReturn(expected).when(supplier).getAsInt();

		doCallRealMethod().when(consumer).thenReturnInt(any());

		assertThat(consumer.thenReturnInt(supplier).applyAsInt(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnLong() {
		double value = this.random.nextDouble();
		long expected = this.random.nextLong();
		DoubleConsumer consumer = mock(DoubleConsumer.class);
		LongSupplier supplier = mock(LongSupplier.class);
		doReturn(expected).when(supplier).getAsLong();

		doCallRealMethod().when(consumer).thenReturnLong(any());

		assertThat(consumer.thenReturnLong(supplier).applyAsLong(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void when() {
		double accepted = this.random.nextDouble();
		double rejected = this.random.nextDouble();
		DoublePredicate predicate = mock(DoublePredicate.class);

		doReturn(true).when(predicate).test(accepted);
		doReturn(false).when(predicate).test(rejected);

		DoubleConsumer whenAccepted = mock(DoubleConsumer.class);
		DoubleConsumer whenRejected = mock(DoubleConsumer.class);

		DoubleConsumer consumer = DoubleConsumer.when(predicate, whenAccepted, whenRejected);

		consumer.accept(accepted);

		verify(whenAccepted, only()).accept(accepted);
		verifyNoInteractions(whenRejected);

		consumer.accept(rejected);

		verify(whenRejected, only()).accept(rejected);
		verifyNoMoreInteractions(whenAccepted);
	}

	@Test
	public void compose() {
		DoubleConsumer consumer = mock(DoubleConsumer.class);
		doCallRealMethod().when(consumer).compose(any());
		ToDoubleFunction<Object> composer = mock(ToDoubleFunction.class);
		Object value = new Object();
		double result = this.random.nextDouble();
		doReturn(result).when(composer).applyAsDouble(value);

		consumer.compose(composer).accept(value);

		verify(consumer).accept(result);
	}

	@Test
	public void composeAsDouble() {
		DoubleConsumer consumer = mock(DoubleConsumer.class);
		doCallRealMethod().when(consumer).composeDouble(any());
		DoubleUnaryOperator composer = mock(DoubleUnaryOperator.class);
		double value = this.random.nextDouble();
		double result = this.random.nextDouble();
		doReturn(result).when(composer).applyAsDouble(value);

		consumer.composeDouble(composer).accept(value);

		verify(consumer).accept(result);
	}

	@Test
	public void composeBinary() {
		DoubleConsumer consumer = mock(DoubleConsumer.class);
		doCallRealMethod().when(consumer).composeBinary(any());
		ToDoubleBiFunction<Object, Object> composer = mock(ToDoubleBiFunction.class);
		Object key = new Object();
		Object value = new Object();
		double result = this.random.nextDouble();
		doReturn(result).when(composer).applyAsDouble(key, value);

		consumer.composeBinary(composer).accept(key, value);

		verify(consumer).accept(result);
	}

	@Test
	public void empty() {
		DoubleConsumer consumer = DoubleConsumer.of();
		consumer.accept(this.random.nextDouble());
	}

	@Test
	public void andThen() {
		double value = this.random.nextDouble();
		DoubleConsumer before = mock(DoubleConsumer.class);
		doCallRealMethod().when(before).andThen(any());
		DoubleConsumer after = mock(DoubleConsumer.class);
		InOrder order = inOrder(before, after);

		before.andThen(after).accept(value);

		order.verify(before).accept(value);
		order.verify(after).accept(value);
	}

	@Test
	public void acceptAll() {
		double value = this.random.nextDouble();
		DoubleConsumer consumer1 = mock(DoubleConsumer.class);
		DoubleConsumer consumer2 = mock(DoubleConsumer.class);
		DoubleConsumer consumer3 = mock(DoubleConsumer.class);
		InOrder order = inOrder(consumer1, consumer2, consumer3);

		DoubleConsumer consumer = DoubleConsumer.of(List.of(consumer1, consumer2, consumer3));

		consumer.accept(value);

		order.verify(consumer1).accept(value);
		order.verify(consumer2).accept(value);
		order.verify(consumer3).accept(value);
	}
}
