/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
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
		int allowed = this.random.nextInt();
		int disallowed = this.random.nextInt();
		IntPredicate predicate = mock(IntPredicate.class);

		doReturn(true).when(predicate).test(allowed);
		doReturn(false).when(predicate).test(disallowed);

		IntConsumer consumer = mock(IntConsumer.class);

		doCallRealMethod().when(consumer).when(any());

		IntConsumer conditional = consumer.when(predicate);

		conditional.accept(disallowed);

		verify(consumer, never()).accept(disallowed);

		conditional.accept(allowed);

		verify(consumer).accept(allowed);
	}

	@Test
	public void withDefault() {
		int allowed = this.random.nextInt();
		int disallowed = this.random.nextInt();
		int defaultValue = this.random.nextInt();

		IntPredicate predicate = mock(IntPredicate.class);

		doReturn(true).when(predicate).test(allowed);
		doReturn(false).when(predicate).test(disallowed);

		IntConsumer consumer = mock(IntConsumer.class);
		IntSupplier defaultProvider = mock(IntSupplier.class);

		doCallRealMethod().when(consumer).withDefault(any(), any());
		doReturn(defaultValue).when(defaultProvider).getAsInt();

		IntConsumer conditional = consumer.withDefault(predicate, defaultProvider);

		conditional.accept(allowed);

		verify(consumer).accept(allowed);
		verifyNoInteractions(defaultProvider);

		conditional.accept(disallowed);

		verify(consumer, never()).accept(disallowed);
		verify(consumer).accept(defaultValue);
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
		doCallRealMethod().when(consumer).composeAsInt(ArgumentMatchers.<IntUnaryOperator>any());
		IntUnaryOperator composer = mock(IntUnaryOperator.class);
		int value = this.random.nextInt();
		int result = this.random.nextInt();
		doReturn(result).when(composer).applyAsInt(value);

		consumer.composeAsInt(composer).accept(value);

		verify(consumer).accept(result);
	}

	@Test
	public void composeBinary() {
		IntConsumer consumer = mock(IntConsumer.class);
		doCallRealMethod().when(consumer).compose(ArgumentMatchers.<ToIntBiFunction<Object, Object>>any());
		ToIntBiFunction<Object, Object> composer = mock(ToIntBiFunction.class);
		Object key = new Object();
		Object value = new Object();
		int result = this.random.nextInt();
		doReturn(result).when(composer).applyAsInt(key, value);

		consumer.compose(composer).accept(key, value);

		verify(consumer).accept(result);
	}

	@Test
	public void empty() {
		IntConsumer consumer = IntConsumer.EMPTY;
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

		IntConsumer consumer = IntConsumer.acceptAll(List.of(consumer1, consumer2, consumer3));

		consumer.accept(value);

		order.verify(consumer1).accept(value);
		order.verify(consumer2).accept(value);
		order.verify(consumer3).accept(value);
	}
}
