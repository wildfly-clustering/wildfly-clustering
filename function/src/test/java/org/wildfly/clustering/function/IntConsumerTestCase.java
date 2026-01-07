/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Random;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

/**
 * Unit test for {@link IntConsumer}.
 * @author Paul Ferraro
 */
public class IntConsumerTestCase {
	private final Random random = new Random();

	@Test
	public void thenReturn() {
		int value = this.random.nextInt();
		Object expected = new Object();
		IntConsumer consumer = mock(IntConsumer.class);
		Supplier<Object> supplier = mock(Supplier.class);
		doReturn(expected).when(supplier).get();

		doCallRealMethod().when(consumer).thenReturn(any());

		assertThat(consumer.thenReturn(supplier).apply(value)).isSameAs(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnBoolean() {
		int value = this.random.nextInt();
		boolean expected = this.random.nextBoolean();
		IntConsumer consumer = mock(IntConsumer.class);
		BooleanSupplier supplier = mock(BooleanSupplier.class);
		doReturn(expected).when(supplier).getAsBoolean();

		doCallRealMethod().when(consumer).thenReturnBoolean(any());

		assertThat(consumer.thenReturnBoolean(supplier).test(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnDouble() {
		int value = this.random.nextInt();
		double expected = this.random.nextDouble();
		IntConsumer consumer = mock(IntConsumer.class);
		DoubleSupplier supplier = mock(DoubleSupplier.class);
		doReturn(expected).when(supplier).getAsDouble();

		doCallRealMethod().when(consumer).thenReturnDouble(any());

		assertThat(consumer.thenReturnDouble(supplier).applyAsDouble(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnInt() {
		int value = this.random.nextInt();
		int expected = this.random.nextInt();
		IntConsumer consumer = mock(IntConsumer.class);
		IntSupplier supplier = mock(IntSupplier.class);
		doReturn(expected).when(supplier).getAsInt();

		doCallRealMethod().when(consumer).thenReturnInt(any());

		assertThat(consumer.thenReturnInt(supplier).applyAsInt(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnLong() {
		int value = this.random.nextInt();
		long expected = this.random.nextLong();
		IntConsumer consumer = mock(IntConsumer.class);
		LongSupplier supplier = mock(LongSupplier.class);
		doReturn(expected).when(supplier).getAsLong();

		doCallRealMethod().when(consumer).thenReturnLong(any());

		assertThat(consumer.thenReturnLong(supplier).applyAsLong(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void when() {
		int accepted = this.random.nextInt();
		int rejected = this.random.nextInt();
		IntPredicate predicate = mock(IntPredicate.class);

		doReturn(true).when(predicate).test(accepted);
		doReturn(false).when(predicate).test(rejected);

		IntConsumer whenAccepted = mock(IntConsumer.class);
		IntConsumer whenRejected = mock(IntConsumer.class);

		IntConsumer conditional = IntConsumer.when(predicate, whenAccepted, whenRejected);

		conditional.accept(accepted);

		verify(whenAccepted, only()).accept(accepted);
		verifyNoInteractions(whenRejected);

		conditional.accept(rejected);

		verify(whenRejected, only()).accept(rejected);
		verifyNoMoreInteractions(whenAccepted);
	}

	@Test
	public void compose() {
		IntConsumer consumer = mock(IntConsumer.class);
		doCallRealMethod().when(consumer).compose(ArgumentMatchers.<ToIntFunction<Object>>any());
		ToIntFunction<Object> composer = mock(ToIntFunction.class);
		Object value = new Object();
		int result = this.random.nextInt();
		doReturn(result).when(composer).applyAsInt(value);

		consumer.compose(composer).accept(value);

		verify(consumer).accept(result);
	}

	@Test
	public void composeAsInt() {
		IntConsumer consumer = mock(IntConsumer.class);
		doCallRealMethod().when(consumer).composeInt(ArgumentMatchers.<IntUnaryOperator>any());
		IntUnaryOperator composer = mock(IntUnaryOperator.class);
		int value = this.random.nextInt();
		int result = this.random.nextInt();
		doReturn(result).when(composer).applyAsInt(value);

		consumer.composeInt(composer).accept(value);

		verify(consumer).accept(result);
	}

	@Test
	public void composeBinary() {
		IntConsumer consumer = mock(IntConsumer.class);
		doCallRealMethod().when(consumer).composeBinary(ArgumentMatchers.<ToIntBiFunction<Object, Object>>any());
		ToIntBiFunction<Object, Object> composer = mock(ToIntBiFunction.class);
		Object key = new Object();
		Object value = new Object();
		int result = this.random.nextInt();
		doReturn(result).when(composer).applyAsInt(key, value);

		consumer.composeBinary(composer).accept(key, value);

		verify(consumer).accept(result);
	}

	@Test
	public void empty() {
		IntConsumer consumer = IntConsumer.of();
		consumer.accept(this.random.nextInt());
	}

	@Test
	public void andThen() {
		int value = this.random.nextInt();
		IntConsumer before = mock(IntConsumer.class);
		doCallRealMethod().when(before).andThen(any());
		IntConsumer after = mock(IntConsumer.class);
		InOrder order = inOrder(before, after);

		before.andThen(after).accept(value);

		order.verify(before).accept(value);
		order.verify(after).accept(value);
	}

	@Test
	public void acceptAll() {
		int value = this.random.nextInt();
		IntConsumer consumer1 = mock(IntConsumer.class);
		IntConsumer consumer2 = mock(IntConsumer.class);
		IntConsumer consumer3 = mock(IntConsumer.class);
		InOrder order = inOrder(consumer1, consumer2, consumer3);

		IntConsumer consumer = IntConsumer.of(List.of(consumer1, consumer2, consumer3));

		consumer.accept(value);

		order.verify(consumer1).accept(value);
		order.verify(consumer2).accept(value);
		order.verify(consumer3).accept(value);
	}
}
