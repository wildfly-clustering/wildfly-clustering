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
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

/**
 * Unit test for {@link DoubleConsumer}.
 * @author Paul Ferraro
 */
public class DoubleConsumerTestCase {
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
		double allowed = this.random.nextDouble();
		double disallowed = this.random.nextDouble();
		DoublePredicate predicate = mock(DoublePredicate.class);

		doReturn(true).when(predicate).test(allowed);
		doReturn(false).when(predicate).test(disallowed);

		DoubleConsumer consumer = mock(DoubleConsumer.class);

		doCallRealMethod().when(consumer).when(any());

		DoubleConsumer conditional = consumer.when(predicate);

		conditional.accept(disallowed);

		verify(consumer, never()).accept(disallowed);

		conditional.accept(allowed);

		verify(consumer).accept(allowed);
	}

	@Test
	public void withDefault() {
		double allowed = this.random.nextDouble();
		double disallowed = this.random.nextDouble();
		double defaultValue = this.random.nextDouble();

		DoublePredicate predicate = mock(DoublePredicate.class);

		doReturn(true).when(predicate).test(allowed);
		doReturn(false).when(predicate).test(disallowed);

		DoubleConsumer consumer = mock(DoubleConsumer.class);
		DoubleSupplier defaultProvider = mock(DoubleSupplier.class);

		doCallRealMethod().when(consumer).withDefault(any(), any());
		doReturn(defaultValue).when(defaultProvider).getAsDouble();

		DoubleConsumer conditional = consumer.withDefault(predicate, defaultProvider);

		conditional.accept(allowed);

		verify(consumer).accept(allowed);
		verifyNoInteractions(defaultProvider);

		conditional.accept(disallowed);

		verify(consumer, never()).accept(disallowed);
		verify(consumer).accept(defaultValue);
	}

	@Test
	public void compose() {
		DoubleConsumer consumer = mock(DoubleConsumer.class);
		doCallRealMethod().when(consumer).compose(ArgumentMatchers.<ToDoubleFunction<Object>>any());
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
		doCallRealMethod().when(consumer).composeAsDouble(ArgumentMatchers.<DoubleUnaryOperator>any());
		DoubleUnaryOperator composer = mock(DoubleUnaryOperator.class);
		double value = this.random.nextDouble();
		double result = this.random.nextDouble();
		doReturn(result).when(composer).applyAsDouble(value);

		consumer.composeAsDouble(composer).accept(value);

		verify(consumer).accept(result);
	}

	@Test
	public void composeBinary() {
		DoubleConsumer consumer = mock(DoubleConsumer.class);
		doCallRealMethod().when(consumer).compose(ArgumentMatchers.<ToDoubleBiFunction<Object, Object>>any());
		ToDoubleBiFunction<Object, Object> composer = mock(ToDoubleBiFunction.class);
		Object key = new Object();
		Object value = new Object();
		double result = this.random.nextDouble();
		doReturn(result).when(composer).applyAsDouble(key, value);

		consumer.compose(composer).accept(key, value);

		verify(consumer).accept(result);
	}

	@Test
	public void empty() {
		DoubleConsumer consumer = DoubleConsumer.EMPTY;
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

		DoubleConsumer consumer = DoubleConsumer.acceptAll(List.of(consumer1, consumer2, consumer3));

		consumer.accept(value);

		order.verify(consumer1).accept(value);
		order.verify(consumer2).accept(value);
		order.verify(consumer3).accept(value);
	}
}
