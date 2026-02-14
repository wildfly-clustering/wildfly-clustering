/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.DoubleFunction;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * Unit test for {@link Predicate}.
 * @author Paul Ferraro
 */
public class PredicateTestCase {
	private final Random random = new Random();

	@Test
	public void test() {
		UUID value = UUID.randomUUID();
		assertThat(Predicate.of(true).test(value)).isTrue();
		assertThat(Predicate.of(true).test(null)).isTrue();
		assertThat(Predicate.of(false).test(value)).isFalse();
		assertThat(Predicate.of(false).test(null)).isFalse();
	}

	@Test
	public void equalTo() {
		UUID value = UUID.randomUUID();
		UUID copy = new UUID(value.getMostSignificantBits(), value.getLeastSignificantBits());
		UUID different = UUID.randomUUID();

		Predicate<UUID> predicate = Predicate.equalTo(value);

		assertThat(predicate.test(value)).isTrue();
		assertThat(predicate.test(copy)).isTrue();
		assertThat(predicate.test(different)).isFalse();
	}

	@Test
	public void identicalTo() {
		UUID value = UUID.randomUUID();
		UUID copy = new UUID(value.getMostSignificantBits(), value.getLeastSignificantBits());
		UUID different = UUID.randomUUID();

		Predicate<UUID> predicate = Predicate.identicalTo(value);

		assertThat(predicate.test(value)).isTrue();
		assertThat(predicate.test(copy)).isFalse();
		assertThat(predicate.test(different)).isFalse();
	}

	@Test
	public void lessThan() {
		int value = this.random.nextInt();

		Predicate<Integer> predicate = Predicate.lessThan(value);

		assertThat(predicate.test(value - 1)).isTrue();
		assertThat(predicate.test(value)).isFalse();
		assertThat(predicate.test(value + 1)).isFalse();
	}

	@Test
	public void greaterThan() {
		int value = this.random.nextInt();

		Predicate<Integer> predicate = Predicate.greaterThan(value);

		assertThat(predicate.test(value - 1)).isFalse();
		assertThat(predicate.test(value)).isFalse();
		assertThat(predicate.test(value + 1)).isTrue();
	}

	@Test
	public void negate() {
		UUID value = UUID.randomUUID();
		assertThat(Predicate.of(true).negate().test(value)).isFalse();
		assertThat(Predicate.of(true).negate().test(null)).isFalse();
		assertThat(Predicate.of(false).negate().test(value)).isTrue();
		assertThat(Predicate.of(false).negate().test(null)).isTrue();
	}

	@Test
	public void compose() {
		Predicate<UUID> predicate = mock(Predicate.class);

		doCallRealMethod().when(predicate).compose(ArgumentMatchers.<Function<UUID, UUID>>any());

		UUID value = UUID.randomUUID();
		UUID mapped = UUID.randomUUID();
		boolean expected = this.random.nextBoolean();

		Function<UUID, UUID> function = mock(Function.class);

		doReturn(mapped).when(function).apply(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.compose(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeDouble() {
		Predicate<UUID> predicate = mock(Predicate.class);

		doCallRealMethod().when(predicate).composeDouble(ArgumentMatchers.<DoubleFunction<UUID>>any());

		double value = this.random.nextDouble();
		UUID mapped = UUID.randomUUID();
		boolean expected = this.random.nextBoolean();

		DoubleFunction<UUID> function = mock(DoubleFunction.class);

		doReturn(mapped).when(function).apply(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeDouble(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeInt() {
		Predicate<UUID> predicate = mock(Predicate.class);

		doCallRealMethod().when(predicate).composeInt(ArgumentMatchers.<IntFunction<UUID>>any());

		int value = this.random.nextInt();
		UUID mapped = UUID.randomUUID();
		boolean expected = this.random.nextBoolean();

		IntFunction<UUID> function = mock(IntFunction.class);

		doReturn(mapped).when(function).apply(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeInt(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeLong() {
		Predicate<UUID> predicate = mock(Predicate.class);

		doCallRealMethod().when(predicate).composeLong(ArgumentMatchers.<LongFunction<UUID>>any());

		long value = this.random.nextLong();
		UUID mapped = UUID.randomUUID();
		boolean expected = this.random.nextBoolean();

		LongFunction<UUID> function = mock(LongFunction.class);

		doReturn(mapped).when(function).apply(value);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeLong(function).test(value)).isEqualTo(expected);
	}

	@Test
	public void composeBinary() {
		Predicate<UUID> predicate = mock(Predicate.class);

		doCallRealMethod().when(predicate).composeBinary(ArgumentMatchers.<BiFunction<UUID, UUID, UUID>>any());

		UUID value1 = UUID.randomUUID();
		UUID value2 = UUID.randomUUID();
		UUID mapped = UUID.randomUUID();
		boolean expected = this.random.nextBoolean();

		BiFunction<UUID, UUID, UUID> function = mock(BiFunction.class);

		doReturn(mapped).when(function).apply(value1, value2);
		doReturn(expected).when(predicate).test(mapped);

		assertThat(predicate.composeBinary(function).test(value1, value2)).isEqualTo(expected);
	}

	@Test
	public void entry() {
		Predicate<UUID> keyPredicate = mock(Predicate.class);
		Predicate<UUID> valuePredicate = mock(Predicate.class);

		Predicate<Map.Entry<UUID, UUID>> predicate = Predicate.entry(keyPredicate, valuePredicate);

		UUID allowedKey = UUID.randomUUID();
		UUID disallowedKey = UUID.randomUUID();
		UUID allowedValue = UUID.randomUUID();
		UUID disallowedValue = UUID.randomUUID();

		doReturn(false).when(keyPredicate).test(disallowedKey);
		doReturn(false).when(valuePredicate).test(disallowedValue);
		doReturn(true).when(keyPredicate).test(allowedKey);
		doReturn(true).when(valuePredicate).test(allowedValue);

		assertThat(predicate.test(Map.entry(allowedKey, allowedValue))).isTrue();
		assertThat(predicate.test(Map.entry(allowedKey, disallowedValue))).isFalse();
		assertThat(predicate.test(Map.entry(disallowedKey, allowedValue))).isFalse();
		assertThat(predicate.test(Map.entry(disallowedKey, disallowedValue))).isFalse();
	}
}
