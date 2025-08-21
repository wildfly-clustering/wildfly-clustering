/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Random;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.ToDoubleFunction;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DoublePredicate}.
 * @author Paul Ferraro
 */
public class DoublePredicateTestCase {
	private final Random random = new Random();

	@Test
	public void test() {
		double positive = this.random.nextDouble() + 1d;
		double negative = this.random.nextDouble() - 1d;
		assertThat(DoublePredicate.ALWAYS.test(negative)).isTrue();
		assertThat(DoublePredicate.ALWAYS.test(0d)).isTrue();
		assertThat(DoublePredicate.ALWAYS.test(positive)).isTrue();

		assertThat(DoublePredicate.NEVER.test(negative)).isFalse();
		assertThat(DoublePredicate.NEVER.test(0d)).isFalse();
		assertThat(DoublePredicate.NEVER.test(positive)).isFalse();

		assertThat(DoublePredicate.of(true).test(negative)).isTrue();
		assertThat(DoublePredicate.of(true).test(0d)).isTrue();
		assertThat(DoublePredicate.of(true).test(positive)).isTrue();

		assertThat(DoublePredicate.of(false).test(negative)).isFalse();
		assertThat(DoublePredicate.of(false).test(0d)).isFalse();
		assertThat(DoublePredicate.of(false).test(positive)).isFalse();

		assertThat(DoublePredicate.NEGATIVE.test(negative)).isTrue();
		assertThat(DoublePredicate.NEGATIVE.test(0d)).isFalse();
		assertThat(DoublePredicate.NEGATIVE.test(positive)).isFalse();

		assertThat(DoublePredicate.ZERO.test(negative)).isFalse();
		assertThat(DoublePredicate.ZERO.test(0d)).isTrue();
		assertThat(DoublePredicate.ZERO.test(positive)).isFalse();

		assertThat(DoublePredicate.POSITIVE.test(negative)).isFalse();
		assertThat(DoublePredicate.POSITIVE.test(0d)).isFalse();
		assertThat(DoublePredicate.POSITIVE.test(positive)).isTrue();
	}

	@Test
	public void equalTo() {
		double value = this.random.nextDouble();
		double different = this.random.nextDouble();

		DoublePredicate predicate = DoublePredicate.equalTo(value);

		assertThat(predicate.test(value)).isTrue();
		assertThat(predicate.test(different)).isFalse();
	}

	@Test
	public void lessThan() {
		double value = this.random.nextDouble();

		DoublePredicate predicate = DoublePredicate.lessThan(value);

		assertThat(predicate.test(value - 1d)).isTrue();
		assertThat(predicate.test(value)).isFalse();
		assertThat(predicate.test(value + 1d)).isFalse();
	}

	@Test
	public void greaterThan() {
		double value = this.random.nextDouble();

		DoublePredicate predicate = DoublePredicate.greaterThan(value);

		assertThat(predicate.test(value - 1d)).isFalse();
		assertThat(predicate.test(value)).isFalse();
		assertThat(predicate.test(value + 1d)).isTrue();
	}

	@Test
	public void negate() {
		double value = this.random.nextDouble();
		assertThat(DoublePredicate.ALWAYS.negate().test(value)).isFalse();
		assertThat(DoublePredicate.NEVER.negate().test(value)).isTrue();
	}

	@Test
	public void compose() {
		DoublePredicate predicate = mock(DoublePredicate.class);

		doCallRealMethod().when(predicate).compose(any());

		UUID value = UUID.randomUUID();
		double mapped = this.random.nextDouble();
		boolean expected = this.random.nextBoolean();

		ToDoubleFunction<UUID> function = mock(ToDoubleFunction.class);

		doReturn(mapped).when(function).applyAsDouble(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.compose(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeDouble() {
		DoublePredicate predicate = mock(DoublePredicate.class);

		doCallRealMethod().when(predicate).composeDouble(any());

		double value = this.random.nextDouble();
		double mapped = this.random.nextDouble();
		boolean expected = this.random.nextBoolean();

		DoubleUnaryOperator function = mock(DoubleUnaryOperator.class);

		doReturn(mapped).when(function).applyAsDouble(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeDouble(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeInt() {
		DoublePredicate predicate = mock(DoublePredicate.class);

		doCallRealMethod().when(predicate).composeInt(any());

		int value = this.random.nextInt();
		double mapped = this.random.nextDouble();
		boolean expected = this.random.nextBoolean();

		IntToDoubleFunction function = mock(IntToDoubleFunction.class);

		doReturn(mapped).when(function).applyAsDouble(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeInt(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeLong() {
		DoublePredicate predicate = mock(DoublePredicate.class);

		doCallRealMethod().when(predicate).composeLong(any());

		long value = this.random.nextLong();
		double mapped = this.random.nextDouble();
		boolean expected = this.random.nextBoolean();

		LongToDoubleFunction function = mock(LongToDoubleFunction.class);

		doReturn(mapped).when(function).applyAsDouble(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeLong(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void xor() {
		DoublePredicate predicate1 = mock(DoublePredicate.class);
		DoublePredicate predicate2 = mock(DoublePredicate.class);

		doCallRealMethod().when(predicate1).xor(any());

		DoublePredicate predicate = predicate1.xor(predicate2);

		double value = this.random.nextDouble();

		doReturn(false, true, false, true).when(predicate1).test(value);
		doReturn(false, false, true, true).when(predicate2).test(value);

		assertThat(predicate.test(value)).isFalse();
		assertThat(predicate.test(value)).isTrue();
		assertThat(predicate.test(value)).isTrue();
		assertThat(predicate.test(value)).isFalse();
	}
}
