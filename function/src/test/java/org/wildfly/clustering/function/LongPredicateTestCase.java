/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Random;
import java.util.UUID;
import java.util.function.DoubleToLongFunction;
import java.util.function.IntToLongFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ToLongFunction;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link LongPredicate}.
 * @author Paul Ferraro
 */
public class LongPredicateTestCase {
	private final Random random = new Random();

	@Test
	public void test() {
		long positive = this.random.nextLong(1L, Integer.MAX_VALUE);
		long negative = this.random.nextLong(Integer.MIN_VALUE, 0L);

		assertThat(LongPredicate.of(true).test(negative)).isTrue();
		assertThat(LongPredicate.of(true).test(0L)).isTrue();
		assertThat(LongPredicate.of(true).test(positive)).isTrue();

		assertThat(LongPredicate.of(false).test(negative)).isFalse();
		assertThat(LongPredicate.of(false).test(0L)).isFalse();
		assertThat(LongPredicate.of(false).test(positive)).isFalse();

		assertThat(LongPredicate.NEGATIVE.test(negative)).isTrue();
		assertThat(LongPredicate.NEGATIVE.test(0L)).isFalse();
		assertThat(LongPredicate.NEGATIVE.test(positive)).isFalse();

		assertThat(LongPredicate.ZERO.test(negative)).isFalse();
		assertThat(LongPredicate.ZERO.test(0L)).isTrue();
		assertThat(LongPredicate.ZERO.test(positive)).isFalse();

		assertThat(LongPredicate.POSITIVE.test(negative)).isFalse();
		assertThat(LongPredicate.POSITIVE.test(0L)).isFalse();
		assertThat(LongPredicate.POSITIVE.test(positive)).isTrue();
	}

	@Test
	public void equalTo() {
		long value = this.random.nextLong();
		long different = this.random.nextLong();

		LongPredicate predicate = LongPredicate.equalTo(value);

		assertThat(predicate.test(value)).isTrue();
		assertThat(predicate.test(different)).isFalse();
	}

	@Test
	public void lessThan() {
		long value = this.random.nextLong();

		LongPredicate predicate = LongPredicate.lessThan(value);

		assertThat(predicate.test(value - 1L)).isTrue();
		assertThat(predicate.test(value)).isFalse();
		assertThat(predicate.test(value + 1L)).isFalse();
	}

	@Test
	public void greaterThan() {
		long value = this.random.nextLong();

		LongPredicate predicate = LongPredicate.greaterThan(value);

		assertThat(predicate.test(value - 1L)).isFalse();
		assertThat(predicate.test(value)).isFalse();
		assertThat(predicate.test(value + 1L)).isTrue();
	}

	@Test
	public void negate() {
		long value = this.random.nextLong();
		assertThat(LongPredicate.of(true).negate().test(value)).isFalse();
		assertThat(LongPredicate.of(false).negate().test(value)).isTrue();
	}

	@Test
	public void compose() {
		LongPredicate predicate = mock(LongPredicate.class);

		doCallRealMethod().when(predicate).compose(any());

		UUID value = UUID.randomUUID();
		long mapped = this.random.nextLong();
		boolean expected = this.random.nextBoolean();

		ToLongFunction<UUID> function = mock(ToLongFunction.class);

		doReturn(mapped).when(function).applyAsLong(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.compose(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeDouble() {
		LongPredicate predicate = mock(LongPredicate.class);

		doCallRealMethod().when(predicate).composeDouble(any());

		double value = this.random.nextDouble();
		long mapped = this.random.nextLong();
		boolean expected = this.random.nextBoolean();

		DoubleToLongFunction function = mock(DoubleToLongFunction.class);

		doReturn(mapped).when(function).applyAsLong(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeDouble(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeInt() {
		LongPredicate predicate = mock(LongPredicate.class);

		doCallRealMethod().when(predicate).composeInt(any());

		int value = this.random.nextInt();
		long mapped = this.random.nextLong();
		boolean expected = this.random.nextBoolean();

		IntToLongFunction function = mock(IntToLongFunction.class);

		doReturn(mapped).when(function).applyAsLong(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeInt(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeLong() {
		LongPredicate predicate = mock(LongPredicate.class);

		doCallRealMethod().when(predicate).composeLong(any());

		long value = this.random.nextLong();
		long mapped = this.random.nextLong();
		boolean expected = this.random.nextBoolean();

		LongUnaryOperator function = mock(LongUnaryOperator.class);

		doReturn(mapped).when(function).applyAsLong(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeLong(function).test(value)).isEqualTo(expected);
	}
}
