/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Random;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DoubleSupplier}.
 * @author Paul Ferraro
 */
public class DoubleSupplierTestCase {
	private final Random random = new Random();

	@Test
	public void apply() {
		double value = this.random.nextDouble();
		double expectedDouble = this.random.nextDouble();
		int expectedInt = this.random.nextInt();
		long expectedLong = this.random.nextLong();
		boolean expectedBoolean = this.random.nextBoolean();
		Object expected = new Object();

		DoubleSupplier supplier = mock(DoubleSupplier.class);

		doCallRealMethod().when(supplier).thenAccept(any());
		doCallRealMethod().when(supplier).thenApply(any());
		doCallRealMethod().when(supplier).thenApplyAsDouble(any());
		doCallRealMethod().when(supplier).thenApplyAsInt(any());
		doCallRealMethod().when(supplier).thenApplyAsLong(any());
		doCallRealMethod().when(supplier).thenTest(any());
		doReturn(value).when(supplier).getAsDouble();

		DoubleFunction<Object> function = mock(DoubleFunction.class);
		DoubleUnaryOperator toDoubleFunction = mock(DoubleUnaryOperator.class);
		DoubleToIntFunction toIntFunction = mock(DoubleToIntFunction.class);
		DoubleToLongFunction toLongFunction = mock(DoubleToLongFunction.class);
		DoublePredicate predicate = mock(DoublePredicate.class);
		DoubleConsumer consumer = mock(DoubleConsumer.class);

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
		long expected = this.random.nextLong();
		assertThat(DoubleSupplier.of(expected).getAsDouble()).isEqualTo(expected);
		assertThat(DoubleSupplier.ZERO.getAsDouble()).isEqualTo(0L);
	}
}
