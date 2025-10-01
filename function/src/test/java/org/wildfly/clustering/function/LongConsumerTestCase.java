/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
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
		Object value = new Object();
		Object expected = new Object();
		Consumer<Object> consumer = mock(Consumer.class);
		Supplier<Object> supplier = mock(Supplier.class);
		doReturn(expected).when(supplier).get();

		doCallRealMethod().when(consumer).thenReturn(any());

		Object result = consumer.thenReturn(supplier).apply(value);

		assertThat(result).isSameAs(expected);
		verify(consumer).accept(value);
	}

	@Test
	public void when() {
		long allowed = this.random.nextLong();
		long disallowed = this.random.nextLong();
		LongPredicate predicate = mock(LongPredicate.class);

		doReturn(true).when(predicate).test(allowed);
		doReturn(false).when(predicate).test(disallowed);

		LongConsumer consumer = mock(LongConsumer.class);

		doCallRealMethod().when(consumer).when(any());

		LongConsumer conditional = consumer.when(predicate);

		conditional.accept(disallowed);

		verify(consumer, never()).accept(disallowed);

		conditional.accept(allowed);

		verify(consumer).accept(allowed);
	}

	@Test
	public void withDefault() {
		long allowed = this.random.nextLong();
		long disallowed = this.random.nextLong();
		long defaultValue = this.random.nextLong();

		LongPredicate predicate = mock(LongPredicate.class);

		doReturn(true).when(predicate).test(allowed);
		doReturn(false).when(predicate).test(disallowed);

		LongConsumer consumer = mock(LongConsumer.class);
		LongSupplier defaultProvider = mock(LongSupplier.class);

		doCallRealMethod().when(consumer).withDefault(any(), any());
		doReturn(defaultValue).when(defaultProvider).getAsLong();

		LongConsumer conditional = consumer.withDefault(predicate, defaultProvider);

		conditional.accept(allowed);

		verify(consumer).accept(allowed);
		verifyNoInteractions(defaultProvider);

		conditional.accept(disallowed);

		verify(consumer, never()).accept(disallowed);
		verify(consumer).accept(defaultValue);
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
		doCallRealMethod().when(consumer).composeAsLong(ArgumentMatchers.<LongUnaryOperator>any());
		LongUnaryOperator composer = mock(LongUnaryOperator.class);
		long value = this.random.nextLong();
		long result = this.random.nextLong();
		doReturn(result).when(composer).applyAsLong(value);

		consumer.composeAsLong(composer).accept(value);

		verify(consumer).accept(result);
	}

	@Test
	public void composeBinary() {
		LongConsumer consumer = mock(LongConsumer.class);
		doCallRealMethod().when(consumer).compose(ArgumentMatchers.<ToLongBiFunction<Object, Object>>any());
		ToLongBiFunction<Object, Object> composer = mock(ToLongBiFunction.class);
		Object key = new Object();
		Object value = new Object();
		long result = this.random.nextLong();
		doReturn(result).when(composer).applyAsLong(key, value);

		consumer.compose(composer).accept(key, value);

		verify(consumer).accept(result);
	}

	@Test
	public void empty() {
		LongConsumer consumer = LongConsumer.EMPTY;
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

		LongConsumer consumer = LongConsumer.acceptAll(List.of(consumer1, consumer2, consumer3));

		consumer.accept(value);

		order.verify(consumer1).accept(value);
		order.verify(consumer2).accept(value);
		order.verify(consumer3).accept(value);
	}
}
