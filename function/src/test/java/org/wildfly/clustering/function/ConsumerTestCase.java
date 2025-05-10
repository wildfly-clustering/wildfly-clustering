/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Unit test for {@link Consumer}.
 * @author Paul Ferraro
 */
public class ConsumerTestCase {

	@Test
	public void map() {
		Consumer<Object> consumer = Mockito.mock(Consumer.class);
		Mockito.doCallRealMethod().when(consumer).map(ArgumentMatchers.any());
		Function<Object, Object> mapper = Mockito.mock(Function.class);
		Object value = new Object();
		Object result = new Object();
		Mockito.doReturn(result).when(mapper).apply(value);

		consumer.map(mapper).accept(value);

		Mockito.verify(consumer).accept(result);
	}

	@Test
	public void empty() {
		Consumer<Object> consumer = Consumer.empty();
		consumer.accept(new Object());
		consumer.accept(null);
	}

	@Test
	public void close() throws Exception {
		AutoCloseable resource = Mockito.mock(AutoCloseable.class);

		Consumer<AutoCloseable> consumer = Consumer.close();

		consumer.accept(resource);

		Mockito.verify(resource).close();

		Mockito.doThrow(new Exception()).when(resource).close();

		// Verify silent close
		consumer.accept(resource);

		IllegalStateException exception = new IllegalStateException();
		Mockito.doThrow(exception).when(resource).close();

		// Verify runtime exception propagated
		Assertions.assertThatThrownBy(resource::close).isSameAs(exception);
	}

	@Test
	public void ofRunnable() {
		Object value = new Object();
		Runnable runnable = Mockito.mock(Runnable.class);
		Consumer<Object> consumer = Consumer.of(runnable);

		consumer.accept(value);

		Mockito.verify(runnable).run();
	}

	@Test
	public void andThen() {
		Object value = new Object();
		Consumer<Object> before = Mockito.mock(Consumer.class);
		Mockito.doCallRealMethod().when(before).andThen(ArgumentMatchers.any());
		Consumer<Object> after = Mockito.mock(Consumer.class);
		InOrder order = Mockito.inOrder(before, after);

		before.andThen(after).accept(value);

		order.verify(before).accept(value);
		order.verify(after).accept(value);
	}

	@Test
	public void composite() {
		Object value = new Object();
		Consumer<Object> consumer1 = Mockito.mock(Consumer.class);
		Consumer<Object> consumer2 = Mockito.mock(Consumer.class);
		Consumer<Object> consumer3 = Mockito.mock(Consumer.class);
		InOrder order = Mockito.inOrder(consumer1, consumer2, consumer3);

		Consumer<Object> consumer = Consumer.of(List.of(consumer1, consumer2, consumer3));

		consumer.accept(value);

		order.verify(consumer1).accept(value);
		order.verify(consumer2).accept(value);
		order.verify(consumer3).accept(value);
	}
}
