/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

/**
 * Unit test for {@link Consumer}.
 * @author Paul Ferraro
 */
public class ConsumerTestCase {

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
	public void throwing() {
		java.io.IOException cause = new java.io.IOException();
		Consumer<java.io.IOException> consumer = Consumer.throwing(java.io.UncheckedIOException::new);

		assertThatThrownBy(() -> consumer.accept(cause)).isExactlyInstanceOf(java.io.UncheckedIOException.class).cause().isSameAs(cause);
	}

	@Test
	public void compose() {
		Consumer<Object> consumer = mock(Consumer.class);
		doCallRealMethod().when(consumer).compose(ArgumentMatchers.<Function<Object, Object>>any());
		Function<Object, Object> mapper = mock(Function.class);
		Object value = new Object();
		Object result = new Object();
		doReturn(result).when(mapper).apply(value);

		consumer.compose(mapper).accept(value);

		verify(consumer).accept(result);
	}

	@Test
	public void composeBinary() {
		Consumer<Object> consumer = mock(Consumer.class);
		doCallRealMethod().when(consumer).compose(ArgumentMatchers.<BiFunction<Object, Object, Object>>any());
		BiFunction<Object, Object, Object> mapper = mock(BiFunction.class);
		Object key = new Object();
		Object value = new Object();
		Object result = new Object();
		doReturn(result).when(mapper).apply(key, value);

		consumer.compose(mapper).accept(key, value);

		verify(consumer).accept(result);
	}

	@Test
	public void empty() {
		Consumer<Object> consumer = Consumer.empty();
		consumer.accept(new Object());
		consumer.accept(null);
	}

	@Test
	public void handle() {
		Consumer<Object> consumer = mock(Consumer.class);
		BiConsumer<Object, RuntimeException> handler = mock(BiConsumer.class);
		doCallRealMethod().when(consumer).handle(any());
		Object goodValue = new Object();
		Object badValue = new Object();
		RuntimeException exception = new RuntimeException();

		doNothing().when(consumer).accept(goodValue);
		doThrow(exception).when(consumer).accept(badValue);

		consumer.handle(handler).accept(goodValue);

		verify(consumer).accept(goodValue);
		verify(handler, never()).accept(any(), any());

		consumer.handle(handler).accept(badValue);

		verify(handler).accept(badValue, exception);
	}

	@Test
	public void close() throws Exception {
		AutoCloseable resource = mock(AutoCloseable.class);

		Consumer<AutoCloseable> consumer = Consumer.close(Consumer.debug());

		consumer.accept(resource);

		verify(resource).close();

		doThrow(new Exception()).when(resource).close();

		// Verify silent close
		consumer.accept(resource);

		IllegalStateException exception = new IllegalStateException();
		doThrow(exception).when(resource).close();

		// Verify runtime exception propagated
		assertThatThrownBy(resource::close).isSameAs(exception);
	}

	@Test
	public void ofRunnable() {
		Object value = new Object();
		Runnable runnable = mock(Runnable.class);
		Consumer<Object> consumer = Consumer.run(runnable);

		consumer.accept(value);

		verify(runnable).run();
	}

	@Test
	public void andThen() {
		Object value = new Object();
		Consumer<Object> before = mock(Consumer.class);
		doCallRealMethod().when(before).andThen(any());
		Consumer<Object> after = mock(Consumer.class);
		InOrder order = inOrder(before, after);

		before.andThen(after).accept(value);

		order.verify(before).accept(value);
		order.verify(after).accept(value);
	}

	@Test
	public void acceptAll() {
		Object value = new Object();
		Consumer<Object> consumer1 = mock(Consumer.class);
		Consumer<Object> consumer2 = mock(Consumer.class);
		Consumer<Object> consumer3 = mock(Consumer.class);
		InOrder order = inOrder(consumer1, consumer2, consumer3);

		Consumer<Object> consumer = Consumer.acceptAll(List.of(consumer1, consumer2, consumer3));

		consumer.accept(value);

		order.verify(consumer1).accept(value);
		order.verify(consumer2).accept(value);
		order.verify(consumer3).accept(value);
	}
}
