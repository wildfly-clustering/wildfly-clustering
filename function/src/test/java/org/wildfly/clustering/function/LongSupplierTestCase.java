/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Random;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ToLongFunction;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link LongSupplier}.
 * @author Paul Ferraro
 */
public class LongSupplierTestCase {
	private final Random random = new Random();

	@Test
	public void apply() {
		long value = this.random.nextLong();
		double expectedDouble = this.random.nextDouble();
		int expectedInt = this.random.nextInt();
		long expectedLong = this.random.nextLong();
		boolean expectedBoolean = this.random.nextBoolean();
		Object expected = new Object();

		LongSupplier supplier = mock(LongSupplier.class);

		doCallRealMethod().when(supplier).thenAccept(any());
		doCallRealMethod().when(supplier).thenApply(any());
		doCallRealMethod().when(supplier).thenApplyAsDouble(any());
		doCallRealMethod().when(supplier).thenApplyAsInt(any());
		doCallRealMethod().when(supplier).thenApplyAsLong(any());
		doCallRealMethod().when(supplier).thenTest(any());
		doReturn(value).when(supplier).getAsLong();

		LongFunction<Object> function = mock(LongFunction.class);
		LongToDoubleFunction toDoubleFunction = mock(LongToDoubleFunction.class);
		LongToIntFunction toIntFunction = mock(LongToIntFunction.class);
		LongUnaryOperator toLongFunction = mock(LongUnaryOperator.class);
		LongPredicate predicate = mock(LongPredicate.class);
		LongConsumer consumer = mock(LongConsumer.class);

		doReturn(expected).when(function).apply(value);
		doReturn(expectedDouble).when(toDoubleFunction).applyAsDouble(value);
		doReturn(expectedInt).when(toIntFunction).applyAsInt(value);
		doReturn(expectedLong).when(toLongFunction).applyAsLong(value);
		doReturn(expectedBoolean).when(predicate).test(value);

		assertThat(supplier.thenApply(function).get()).isSameAs(expected);
		assertThat(supplier.thenApplyAsDouble(toDoubleFunction).getAsDouble()).isEqualTo(expectedDouble);
		assertThat(supplier.thenApplyAsInt(toIntFunction).getAsInt()).isEqualTo(expectedInt);
		assertThat(supplier.thenApplyAsLong(toLongFunction).getAsLong()).isEqualTo(expectedLong);
		assertThat(supplier.thenTest(predicate).getAsBoolean()).isEqualTo(expectedBoolean);

		supplier.thenAccept(consumer).run();

		verify(consumer).accept(value);
	}

	@Test
	public void handle() {
		long value = this.random.nextLong();

		LongSupplier supplier = mock(LongSupplier.class);

		doCallRealMethod().when(supplier).handle(any());
		doReturn(value).when(supplier).getAsLong();

		ToLongFunction<RuntimeException> handler = mock(ToLongFunction.class);
		long handled = this.random.nextLong();
		RuntimeException exception = new RuntimeException();

		assertThat(supplier.handle(handler).getAsLong()).isEqualTo(value);

		verify(supplier).getAsLong();
		verify(handler, never()).applyAsLong(any());

		doThrow(exception).when(supplier).getAsLong();
		doReturn(handled).when(handler).applyAsLong(exception);

		assertThat(supplier.handle(handler).getAsLong()).isEqualTo(handled);

		verify(supplier, times(2)).getAsLong();
		verify(handler).applyAsLong(any());
	}

	@Test
	public void of() {
		long expected = this.random.nextLong();
		assertThat(LongSupplier.of(expected).getAsLong()).isEqualTo(expected);
		assertThat(LongSupplier.ZERO.getAsLong()).isEqualTo(0L);
		assertThat(LongSupplier.MINIMUM.getAsLong()).isEqualTo(Long.MIN_VALUE);
		assertThat(LongSupplier.MAXIMUM.getAsLong()).isEqualTo(Long.MAX_VALUE);
	}
}
