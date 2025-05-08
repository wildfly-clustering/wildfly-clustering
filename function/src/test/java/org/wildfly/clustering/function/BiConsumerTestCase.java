/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

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
	public void former() {
		Consumer<Object> consumer = Mockito.mock(Consumer.class);

		BiConsumer.former(consumer).accept(this.value1, this.value2);

		Mockito.verify(consumer).accept(this.value1);
	}

	@Test
	public void latter() {
		Consumer<Object> consumer = Mockito.mock(Consumer.class);

		BiConsumer.latter(consumer).accept(this.value1, this.value2);

		Mockito.verify(consumer).accept(this.value2);
	}

	@Test
	public void compose() {
		Consumer<Object> consumer1 = Mockito.mock(Consumer.class);
		Consumer<Object> consumer2 = Mockito.mock(Consumer.class);
		InOrder order = Mockito.inOrder(consumer1, consumer2);

		BiConsumer.of(consumer1, consumer2).accept(this.value1, this.value2);

		order.verify(consumer1).accept(this.value1);
		order.verify(consumer2).accept(this.value2);
	}
}
