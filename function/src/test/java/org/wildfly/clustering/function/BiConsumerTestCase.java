/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Unit test for {@link BiConsumer}.
 * @author Paul Ferraro
 */
public class BiConsumerTestCase {
	private Object value1 = new Object();
	private Object value2 = new Object();

	@Test
	public void empty() {
		BiConsumer<Object, Object> consumer = BiConsumer.empty();
		consumer.accept(this.value1, this.value2);
		consumer.accept(this.value1, null);
		consumer.accept(null, this.value2);
		consumer.accept(null, null);
	}

	@Test
	public void thenReturn() {
		Object value1 = new Object();
		Object value2 = new Object();
		Object expected = new Object();
		BiConsumer<Object, Object> consumer = mock(BiConsumer.class);
		Supplier<Object> supplier = mock(Supplier.class);
		doReturn(expected).when(supplier).get();

		doCallRealMethod().when(consumer).thenReturn(any());

		Object result = consumer.thenReturn(supplier).apply(value1, value2);

		assertThat(result).isSameAs(expected);
		verify(consumer).accept(value1, value2);
	}

	@Test
	public void throwing() {
		String message = "test";
		java.io.IOException cause = new java.io.IOException();
		BiConsumer<String, java.io.IOException> consumer = BiConsumer.throwing(java.io.UncheckedIOException::new);

		assertThatThrownBy(() -> consumer.accept(message, cause)).isExactlyInstanceOf(java.io.UncheckedIOException.class).hasMessage(message).cause().isSameAs(cause);
	}

	@Test
	public void compose() {
		BiConsumer<Object, Object> consumer = Mockito.mock(BiConsumer.class);
		Mockito.doCallRealMethod().when(consumer).compose(ArgumentMatchers.any(), ArgumentMatchers.any());
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
	public void andThen() {
		BiConsumer<Object, Object> before = Mockito.mock(BiConsumer.class);
		BiConsumer<Object, Object> after = Mockito.mock(BiConsumer.class);
		Mockito.doCallRealMethod().when(before).andThen(ArgumentMatchers.any());
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
	public void handle() {
		BiConsumer<Object, Object> consumer = Mockito.mock(BiConsumer.class);
		Consumer<RuntimeException> handler = Mockito.mock(Consumer.class);
		Mockito.doCallRealMethod().when(consumer).handle(ArgumentMatchers.any());
		RuntimeException exception = new RuntimeException();

		consumer.handle(handler).accept(this.value1, this.value2);

		verify(consumer).accept(this.value1, this.value2);
		verify(handler, Mockito.never()).accept(exception);

		doThrow(exception).when(consumer).accept(this.value1, this.value2);

		consumer.handle(handler).accept(this.value1, this.value2);

		verify(handler).accept(exception);
	}

	@Test
	public void composite() {
		BiConsumer<Object, Object> consumer1 = Mockito.mock(BiConsumer.class);
		BiConsumer<Object, Object> consumer2 = Mockito.mock(BiConsumer.class);
		BiConsumer<Object, Object> consumer3 = Mockito.mock(BiConsumer.class);
		InOrder order = Mockito.inOrder(consumer1, consumer2, consumer3);

		BiConsumer.acceptAll(List.of(consumer1, consumer2, consumer3)).accept(this.value1, this.value2);

		order.verify(consumer1).accept(this.value1, this.value2);
		order.verify(consumer2).accept(this.value1, this.value2);
		order.verify(consumer3).accept(this.value1, this.value2);
	}

	@Test
	public void acceptFormer() {
		Consumer<Object> consumer = Mockito.mock(Consumer.class);

		BiConsumer.acceptFormer(consumer).accept(this.value1, this.value2);

		Mockito.verify(consumer).accept(this.value1);
	}

	@Test
	public void acceptLatter() {
		Consumer<Object> consumer = Mockito.mock(Consumer.class);

		BiConsumer.acceptLatter(consumer).accept(this.value1, this.value2);

		Mockito.verify(consumer).accept(this.value2);
	}

	@Test
	public void of() {
		Consumer<Object> consumer1 = Mockito.mock(Consumer.class);
		Consumer<Object> consumer2 = Mockito.mock(Consumer.class);
		InOrder order = Mockito.inOrder(consumer1, consumer2);

		BiConsumer.of(consumer1, consumer2).accept(this.value1, this.value2);

		order.verify(consumer1).accept(this.value1);
		order.verify(consumer2).accept(this.value2);
	}
}
