/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Random;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link IntSupplier}.
 * @author Paul Ferraro
 */
public class IntSupplierTestCase {
	private final Random random = new Random();

	@Test
	public void apply() {
		int value = this.random.nextInt();
		double expectedDouble = this.random.nextDouble();
		int expectedInt = this.random.nextInt();
		long expectedLong = this.random.nextLong();
		boolean expectedBoolean = this.random.nextBoolean();
		Object expected = new Object();

		IntSupplier supplier = mock(IntSupplier.class);

		doCallRealMethod().when(supplier).thenAccept(any());
		doCallRealMethod().when(supplier).thenApply(any());
		doCallRealMethod().when(supplier).thenApplyAsDouble(any());
		doCallRealMethod().when(supplier).thenApplyAsInt(any());
		doCallRealMethod().when(supplier).thenApplyAsLong(any());
		doCallRealMethod().when(supplier).thenTest(any());
		doReturn(value).when(supplier).getAsInt();

		IntFunction<Object> function = mock(IntFunction.class);
		IntToDoubleFunction toDoubleFunction = mock(IntToDoubleFunction.class);
		IntUnaryOperator toIntFunction = mock(IntUnaryOperator.class);
		IntToLongFunction toLongFunction = mock(IntToLongFunction.class);
		IntPredicate predicate = mock(IntPredicate.class);
		IntConsumer consumer = mock(IntConsumer.class);

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
	public void of() {
		int expected = this.random.nextInt();
		assertThat(IntSupplier.of(expected).getAsInt()).isEqualTo(expected);
		assertThat(IntSupplier.ZERO.getAsInt()).isEqualTo(0);
		assertThat(IntSupplier.MINIMUM.getAsInt()).isEqualTo(Integer.MIN_VALUE);
		assertThat(IntSupplier.MAXIMUM.getAsInt()).isEqualTo(Integer.MAX_VALUE);
	}
}
