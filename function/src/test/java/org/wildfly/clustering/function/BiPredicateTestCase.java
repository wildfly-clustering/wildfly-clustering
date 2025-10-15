/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
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

		BiPredicate<UUID, UUID> predicate = BiPredicate.testFormer(predicate1);

		assertThat(predicate.test(this.value1, this.value2)).isEqualTo(expected);

		verify(predicate1, only()).test(this.value1);
	}

	@Test
	public void latter() {
		Predicate<UUID> predicate2 = mock(Predicate.class);
		boolean expected = this.random.nextBoolean();

		doReturn(expected).when(predicate2).test(this.value2);

		BiPredicate<UUID, UUID> predicate = BiPredicate.testLatter(predicate2);

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
}
