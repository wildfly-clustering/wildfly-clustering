/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BiPredicate}.
 * @author Paul Ferraro
 */
public class BiPredicateTestCase {
	private final UUID value1 = UUID.randomUUID();
	private final UUID value2 = UUID.randomUUID();
	private final Random random = new Random();

	@Test
	public void former() {
		Predicate<UUID> predicate1 = mock(Predicate.class);
		boolean expected = this.random.nextBoolean();

		doReturn(expected).when(predicate1).test(this.value1);

		BiPredicate<UUID, UUID> predicate = BiPredicate.former(predicate1);

		assertThat(predicate.test(this.value1, this.value2)).isEqualTo(expected);

		verify(predicate1, only()).test(this.value1);
	}

	@Test
	public void latter() {
		Predicate<UUID> predicate2 = mock(Predicate.class);
		boolean expected = this.random.nextBoolean();

		doReturn(expected).when(predicate2).test(this.value2);

		BiPredicate<UUID, UUID> predicate = BiPredicate.latter(predicate2);

		assertThat(predicate.test(this.value1, this.value2)).isEqualTo(expected);

		verify(predicate2, only()).test(this.value2);
	}

	@Test
	public void negate() {
		BiPredicate<UUID, UUID> predicate = mock(BiPredicate.class);
		boolean expected = this.random.nextBoolean();

		doCallRealMethod().when(predicate).negate();
		doReturn(expected).when(predicate).test(this.value1, this.value2);

		BiPredicate<UUID, UUID> negative = predicate.negate();

		assertThat(negative.test(this.value1, this.value2)).isNotEqualTo(expected);

		verify(predicate).test(this.value1, this.value2);
	}

	@Test
	public void and() {
		Predicate<UUID> predicate1 = mock(Predicate.class);
		Predicate<UUID> predicate2 = mock(Predicate.class);

		boolean expected1 = this.random.nextBoolean();
		boolean expected2 = this.random.nextBoolean();

		doReturn(expected1).when(predicate1).test(this.value1);
		doReturn(expected2).when(predicate2).test(this.value2);

		BiPredicate<UUID, UUID> predicate = BiPredicate.and(predicate1, predicate2);

		assertThat(predicate.test(this.value1, this.value2)).isEqualTo(expected1 && expected2);

		verify(predicate1, only()).test(this.value1);
		if (expected1) {
			verify(predicate2, only()).test(this.value2);
		}
	}

	@Test
	public void or() {
		Predicate<UUID> predicate1 = mock(Predicate.class);
		Predicate<UUID> predicate2 = mock(Predicate.class);

		boolean expected1 = this.random.nextBoolean();
		boolean expected2 = this.random.nextBoolean();

		doReturn(expected1).when(predicate1).test(this.value1);
		doReturn(expected2).when(predicate2).test(this.value2);

		BiPredicate<UUID, UUID> predicate = BiPredicate.or(predicate1, predicate2);

		assertThat(predicate.test(this.value1, this.value2)).isEqualTo(expected1 || expected2);

		verify(predicate1, only()).test(this.value1);
		if (!expected1) {
			verify(predicate2, only()).test(this.value2);
		}
	}

	@Test
	public void compose() {
		BiPredicate<UUID, UUID> after = mock(BiPredicate.class);
		UnaryOperator<UUID> before1 = mock(UnaryOperator.class);
		UnaryOperator<UUID> before2 = mock(UnaryOperator.class);
		doCallRealMethod().when(after).compose(any(), any());

		BiPredicate<UUID, UUID> predicate = after.compose(before1, before2);

		UUID interrimValue1 = UUID.randomUUID();
		UUID interrimValue2 = UUID.randomUUID();
		boolean expected = this.random.nextBoolean();

		doReturn(interrimValue1).when(before1).apply(this.value1);
		doReturn(interrimValue2).when(before2).apply(this.value2);
		doReturn(expected).when(after).test(interrimValue1, interrimValue2);

		assertThat(predicate.test(this.value1, this.value2)).isEqualTo(expected);
	}

	@Test
	public void composeUnary() {
		BiPredicate<UUID, UUID> after = mock(BiPredicate.class);
		UnaryOperator<UUID> before1 = mock(UnaryOperator.class);
		UnaryOperator<UUID> before2 = mock(UnaryOperator.class);
		doCallRealMethod().when(after).composeUnary(any(), any());

		Predicate<UUID> predicate = after.composeUnary(before1, before2);

		UUID interrimValue1 = UUID.randomUUID();
		UUID interrimValue2 = UUID.randomUUID();
		boolean expected = this.random.nextBoolean();

		doReturn(interrimValue1).when(before1).apply(this.value1);
		doReturn(interrimValue2).when(before2).apply(this.value1);
		doReturn(expected).when(after).test(interrimValue1, interrimValue2);

		assertThat(predicate.test(this.value1)).isEqualTo(expected);
	}
}
