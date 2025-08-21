/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Random;
import java.util.UUID;
import java.util.function.DoubleToIntFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongToIntFunction;
import java.util.function.ToIntFunction;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link IntPredicate}.
 * @author Paul Ferraro
 */
public class IntPredicateTestCase {
	private final Random random = new Random();

	@Test
	public void test() {
		int positive = this.random.nextInt(1, Integer.MAX_VALUE);
		int negative = this.random.nextInt(Integer.MIN_VALUE, 0);

		assertThat(IntPredicate.ALWAYS.test(negative)).isTrue();
		assertThat(IntPredicate.ALWAYS.test(0)).isTrue();
		assertThat(IntPredicate.ALWAYS.test(positive)).isTrue();

		assertThat(IntPredicate.NEVER.test(negative)).isFalse();
		assertThat(IntPredicate.NEVER.test(0)).isFalse();
		assertThat(IntPredicate.NEVER.test(positive)).isFalse();

		assertThat(IntPredicate.of(true).test(negative)).isTrue();
		assertThat(IntPredicate.of(true).test(0)).isTrue();
		assertThat(IntPredicate.of(true).test(positive)).isTrue();

		assertThat(IntPredicate.of(false).test(negative)).isFalse();
		assertThat(IntPredicate.of(false).test(0)).isFalse();
		assertThat(IntPredicate.of(false).test(positive)).isFalse();

		assertThat(IntPredicate.NEGATIVE.test(negative)).isTrue();
		assertThat(IntPredicate.NEGATIVE.test(0)).isFalse();
		assertThat(IntPredicate.NEGATIVE.test(positive)).isFalse();

		assertThat(IntPredicate.ZERO.test(negative)).isFalse();
		assertThat(IntPredicate.ZERO.test(0)).isTrue();
		assertThat(IntPredicate.ZERO.test(positive)).isFalse();

		assertThat(IntPredicate.POSITIVE.test(negative)).isFalse();
		assertThat(IntPredicate.POSITIVE.test(0)).isFalse();
		assertThat(IntPredicate.POSITIVE.test(positive)).isTrue();
	}

	@Test
	public void equalTo() {
		int value = this.random.nextInt();
		int different = this.random.nextInt();

		IntPredicate predicate = IntPredicate.equalTo(value);

		assertThat(predicate.test(value)).isTrue();
		assertThat(predicate.test(different)).isFalse();
	}

	@Test
	public void lessThan() {
		int value = this.random.nextInt();

		IntPredicate predicate = IntPredicate.lessThan(value);

		assertThat(predicate.test(value - 1)).isTrue();
		assertThat(predicate.test(value)).isFalse();
		assertThat(predicate.test(value + 1)).isFalse();
	}

	@Test
	public void greaterThan() {
		int value = this.random.nextInt();

		IntPredicate predicate = IntPredicate.greaterThan(value);

		assertThat(predicate.test(value - 1)).isFalse();
		assertThat(predicate.test(value)).isFalse();
		assertThat(predicate.test(value + 1)).isTrue();
	}

	@Test
	public void negate() {
		int value = this.random.nextInt();
		assertThat(IntPredicate.ALWAYS.negate().test(value)).isFalse();
		assertThat(IntPredicate.NEVER.negate().test(value)).isTrue();
	}

	@Test
	public void compose() {
		IntPredicate predicate = mock(IntPredicate.class);

		doCallRealMethod().when(predicate).compose(any());

		UUID value = UUID.randomUUID();
		int mapped = this.random.nextInt();
		boolean expected = this.random.nextBoolean();

		ToIntFunction<UUID> function = mock(ToIntFunction.class);

		doReturn(mapped).when(function).applyAsInt(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.compose(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeDouble() {
		IntPredicate predicate = mock(IntPredicate.class);

		doCallRealMethod().when(predicate).composeDouble(any());

		double value = this.random.nextDouble();
		int mapped = this.random.nextInt();
		boolean expected = this.random.nextBoolean();

		DoubleToIntFunction function = mock(DoubleToIntFunction.class);

		doReturn(mapped).when(function).applyAsInt(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeDouble(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeInt() {
		IntPredicate predicate = mock(IntPredicate.class);

		doCallRealMethod().when(predicate).composeInt(any());

		int value = this.random.nextInt();
		int mapped = this.random.nextInt();
		boolean expected = this.random.nextBoolean();

		IntUnaryOperator function = mock(IntUnaryOperator.class);

		doReturn(mapped).when(function).applyAsInt(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeInt(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeLong() {
		IntPredicate predicate = mock(IntPredicate.class);

		doCallRealMethod().when(predicate).composeLong(any());

		long value = this.random.nextLong();
		int mapped = this.random.nextInt();
		boolean expected = this.random.nextBoolean();

		LongToIntFunction function = mock(LongToIntFunction.class);

		doReturn(mapped).when(function).applyAsInt(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeLong(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void xor() {
		IntPredicate predicate1 = mock(IntPredicate.class);
		IntPredicate predicate2 = mock(IntPredicate.class);

		doCallRealMethod().when(predicate1).xor(any());

		IntPredicate predicate = predicate1.xor(predicate2);

		int value = this.random.nextInt();

		doReturn(false, true, false, true).when(predicate1).test(value);
		doReturn(false, false, true, true).when(predicate2).test(value);

		assertThat(predicate.test(value)).isFalse();
		assertThat(predicate.test(value)).isTrue();
		assertThat(predicate.test(value)).isTrue();
		assertThat(predicate.test(value)).isFalse();
	}
}
