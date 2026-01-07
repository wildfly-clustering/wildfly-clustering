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
import java.util.function.LongUnaryOperator;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

/**
 * Unit test for {@link LongConsumer}.
 * @author Paul Ferraro
 */
public class LongConsumerTestCase {
	private final Random random = new Random();

	@Test
	public void thenReturn() {
		long value = this.random.nextLong();
		Object expected = new Object();
		LongConsumer consumer = mock(LongConsumer.class);
		Supplier<Object> supplier = mock(Supplier.class);
		doReturn(expected).when(supplier).get();

		doCallRealMethod().when(consumer).thenReturn(any());

		assertThat(consumer.thenReturn(supplier).apply(value)).isSameAs(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnBoolean() {
		long value = this.random.nextLong();
		boolean expected = this.random.nextBoolean();
		LongConsumer consumer = mock(LongConsumer.class);
		BooleanSupplier supplier = mock(BooleanSupplier.class);
		doReturn(expected).when(supplier).getAsBoolean();

		doCallRealMethod().when(consumer).thenReturnBoolean(any());

		assertThat(consumer.thenReturnBoolean(supplier).test(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnDouble() {
		long value = this.random.nextLong();
		double expected = this.random.nextDouble();
		LongConsumer consumer = mock(LongConsumer.class);
		DoubleSupplier supplier = mock(DoubleSupplier.class);
		doReturn(expected).when(supplier).getAsDouble();

		doCallRealMethod().when(consumer).thenReturnDouble(any());

		assertThat(consumer.thenReturnDouble(supplier).applyAsDouble(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnInt() {
		long value = this.random.nextLong();
		int expected = this.random.nextInt();
		LongConsumer consumer = mock(LongConsumer.class);
		IntSupplier supplier = mock(IntSupplier.class);
		doReturn(expected).when(supplier).getAsInt();

		doCallRealMethod().when(consumer).thenReturnInt(any());

		assertThat(consumer.thenReturnInt(supplier).applyAsInt(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnLong() {
		long value = this.random.nextLong();
		long expected = this.random.nextLong();
		LongConsumer consumer = mock(LongConsumer.class);
		LongSupplier supplier = mock(LongSupplier.class);
		doReturn(expected).when(supplier).getAsLong();

		doCallRealMethod().when(consumer).thenReturnLong(any());

		assertThat(consumer.thenReturnLong(supplier).applyAsLong(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void when() {
		long accepted = this.random.nextLong();
		long rejected = this.random.nextLong();
		LongPredicate predicate = mock(LongPredicate.class);

		doReturn(true).when(predicate).test(accepted);
		doReturn(false).when(predicate).test(rejected);

		LongConsumer whenAccepted = mock(LongConsumer.class);
		LongConsumer whenRejected = mock(LongConsumer.class);

		LongConsumer conditional = LongConsumer.when(predicate, whenAccepted, whenRejected);

		conditional.accept(accepted);

		verify(whenAccepted, only()).accept(accepted);
		verifyNoInteractions(whenRejected);

		conditional.accept(rejected);

		verify(whenRejected, only()).accept(rejected);
		verifyNoMoreInteractions(whenAccepted);
	}

	@Test
	public void compose() {
		LongConsumer consumer = mock(LongConsumer.class);
		doCallRealMethod().when(consumer).compose(ArgumentMatchers.<ToLongFunction<Object>>any());
		ToLongFunction<Object> composer = mock(ToLongFunction.class);
		Object value = new Object();
		long result = this.random.nextLong();
		doReturn(result).when(composer).applyAsLong(value);

		consumer.compose(composer).accept(value);

		verify(consumer).accept(result);
	}

	@Test
	public void composeAsLong() {
		LongConsumer consumer = mock(LongConsumer.class);
		doCallRealMethod().when(consumer).composeLong(ArgumentMatchers.<LongUnaryOperator>any());
		LongUnaryOperator composer = mock(LongUnaryOperator.class);
		long value = this.random.nextLong();
		long result = this.random.nextLong();
		doReturn(result).when(composer).applyAsLong(value);

		consumer.composeLong(composer).accept(value);

		verify(consumer).accept(result);
	}

	@Test
	public void composeBinary() {
		LongConsumer consumer = mock(LongConsumer.class);
		doCallRealMethod().when(consumer).composeBinary(ArgumentMatchers.<ToLongBiFunction<Object, Object>>any());
		ToLongBiFunction<Object, Object> composer = mock(ToLongBiFunction.class);
		Object key = new Object();
		Object value = new Object();
		long result = this.random.nextLong();
		doReturn(result).when(composer).applyAsLong(key, value);

		consumer.composeBinary(composer).accept(key, value);

		verify(consumer).accept(result);
	}

	@Test
	public void empty() {
		LongConsumer consumer = LongConsumer.of();
		consumer.accept(this.random.nextLong());
	}

	@Test
	public void andThen() {
		long value = this.random.nextLong();
		LongConsumer before = mock(LongConsumer.class);
		doCallRealMethod().when(before).andThen(any());
		LongConsumer after = mock(LongConsumer.class);
		InOrder order = inOrder(before, after);

		before.andThen(after).accept(value);

		order.verify(before).accept(value);
		order.verify(after).accept(value);
	}

	@Test
	public void acceptAll() {
		long value = this.random.nextLong();
		LongConsumer consumer1 = mock(LongConsumer.class);
		LongConsumer consumer2 = mock(LongConsumer.class);
		LongConsumer consumer3 = mock(LongConsumer.class);
		InOrder order = inOrder(consumer1, consumer2, consumer3);

		LongConsumer consumer = LongConsumer.of(List.of(consumer1, consumer2, consumer3));

		consumer.accept(value);

		order.verify(consumer1).accept(value);
		order.verify(consumer2).accept(value);
		order.verify(consumer3).accept(value);
	}
}
