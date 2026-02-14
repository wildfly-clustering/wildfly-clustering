/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

/**
 * Unit test for {@link Consumer}.
 * @author Paul Ferraro
 */
public class ConsumerTestCase {
	private final Random random = new Random();

	@Test
	public void thenReturn() {
		Object value = new Object();
		Object expected = new Object();
		Consumer<Object> consumer = mock(Consumer.class);
		Supplier<Object> supplier = mock(Supplier.class);
		doReturn(expected).when(supplier).get();

		doCallRealMethod().when(consumer).thenReturn(any());

		assertThat(consumer.thenReturn(supplier).apply(value)).isSameAs(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnBoolean() {
		Object value = new Object();
		boolean expected = this.random.nextBoolean();
		Consumer<Object> consumer = mock(Consumer.class);
		BooleanSupplier supplier = mock(BooleanSupplier.class);
		doReturn(expected).when(supplier).getAsBoolean();

		doCallRealMethod().when(consumer).thenReturnBoolean(any());

		assertThat(consumer.thenReturnBoolean(supplier).test(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnDouble() {
		Object value = new Object();
		double expected = this.random.nextDouble();
		Consumer<Object> consumer = mock(Consumer.class);
		DoubleSupplier supplier = mock(DoubleSupplier.class);
		doReturn(expected).when(supplier).getAsDouble();

		doCallRealMethod().when(consumer).thenReturnDouble(any());

		assertThat(consumer.thenReturnDouble(supplier).applyAsDouble(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnInt() {
		Object value = new Object();
		int expected = this.random.nextInt();
		Consumer<Object> consumer = mock(Consumer.class);
		IntSupplier supplier = mock(IntSupplier.class);
		doReturn(expected).when(supplier).getAsInt();

		doCallRealMethod().when(consumer).thenReturnInt(any());

		assertThat(consumer.thenReturnInt(supplier).applyAsInt(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void thenReturnLong() {
		Object value = new Object();
		long expected = this.random.nextLong();
		Consumer<Object> consumer = mock(Consumer.class);
		LongSupplier supplier = mock(LongSupplier.class);
		doReturn(expected).when(supplier).getAsLong();

		doCallRealMethod().when(consumer).thenReturnLong(any());

		assertThat(consumer.thenReturnLong(supplier).applyAsLong(value)).isEqualTo(expected);

		verify(consumer).accept(value);
	}

	@Test
	public void when() {
		UUID accepted = UUID.randomUUID();
		UUID rejected = UUID.randomUUID();
		Predicate<UUID> predicate = mock(Predicate.class);

		doReturn(true).when(predicate).test(accepted);
		doReturn(false).when(predicate).test(rejected);

		Consumer<UUID> whenAccepted = mock(Consumer.class);
		Consumer<UUID> whenRejected = mock(Consumer.class);

		Consumer<UUID> consumer = Consumer.when(predicate, whenAccepted, whenRejected);

		consumer.accept(accepted);

		verify(whenAccepted, only()).accept(accepted);
		verifyNoInteractions(whenRejected);

		consumer.accept(rejected);

		verify(whenRejected, only()).accept(rejected);
		verifyNoMoreInteractions(whenAccepted);
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
		doCallRealMethod().when(consumer).composeBinary(ArgumentMatchers.<BiFunction<Object, Object, Object>>any());
		BiFunction<Object, Object, Object> mapper = mock(BiFunction.class);
		Object key = new Object();
		Object value = new Object();
		Object result = new Object();
		doReturn(result).when(mapper).apply(key, value);

		consumer.composeBinary(mapper).accept(key, value);

		verify(consumer).accept(result);
	}

	@Test
	public void empty() {
		Consumer<Object> consumer = Consumer.of();
		consumer.accept(new Object());
		consumer.accept(null);
	}

	@Test
	public void close() throws Exception {
		AutoCloseable resource = mock(AutoCloseable.class);
		java.util.function.Consumer<Exception> handler = mock(java.util.function.Consumer.class);

		Consumer<AutoCloseable> consumer = Consumer.close(handler);

		consumer.accept(resource);

		verify(resource).close();
		verifyNoInteractions(handler);

		Exception checkedException = new Exception();
		RuntimeException uncheckedException = new RuntimeException();

		doThrow(checkedException, uncheckedException).when(resource).close();

		// Verify silent close
		consumer.accept(resource);

		verify(handler).accept(checkedException);

		// Verify runtime exception propagated
		assertThatThrownBy(resource::close).isSameAs(uncheckedException);

		verifyNoMoreInteractions(handler);
	}

	@Test
	public void ofRunnable() {
		Object value = new Object();
		Runnable runnable = mock(Runnable.class);
		Consumer<Object> consumer = Consumer.of(Consumer.of(), runnable);

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

		Consumer<Object> consumer = Consumer.of(List.of(consumer1, consumer2, consumer3));

		consumer.accept(value);

		order.verify(consumer1).accept(value);
		order.verify(consumer2).accept(value);
		order.verify(consumer3).accept(value);
	}
}
