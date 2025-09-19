/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BiPredicate}.
 * @author Paul Ferraro
 */
public class BiPredicateTestCase {
	private Object value1 = new Object();
	private Object value2 = new Object();

	@Test
	public void former() {
		Predicate<Object> predicate1 = mock(Predicate.class);
		doReturn(false, true).when(predicate1).test(this.value1);

		BiPredicate<Object, Object> predicate = BiPredicate.testFormer(predicate1);

		assertThat(predicate.test(this.value1, this.value2)).isFalse();
		assertThat(predicate.test(this.value1, this.value2)).isTrue();
	}

	@Test
	public void latter() {
		Predicate<Object> predicate2 = mock(Predicate.class);
		doReturn(false, true).when(predicate2).test(this.value2);

		BiPredicate<Object, Object> predicate = BiPredicate.testLatter(predicate2);

		assertThat(predicate.test(this.value1, this.value2)).isFalse();
		assertThat(predicate.test(this.value1, this.value2)).isTrue();
	}

	@Test
	public void negate() {
		BiPredicate<Object, Object> predicate = mock(BiPredicate.class);
		doCallRealMethod().when(predicate).negate();
		doReturn(false, true).when(predicate).test(this.value1, this.value2);
		BiPredicate<Object, Object> negative = predicate.negate();
		assertThat(negative.test(this.value1, this.value2)).isTrue();
		assertThat(negative.test(this.value1, this.value2)).isFalse();
	}

	@Test
	public void and() {
		Predicate<Object> predicate1 = mock(Predicate.class);
		Predicate<Object> predicate2 = mock(Predicate.class);
		doReturn(false, false, true, true).when(predicate1).test(this.value1);
		doReturn(false, true, false, true).when(predicate2).test(this.value2);

		BiPredicate<Object, Object> predicate = BiPredicate.and(predicate1, predicate2);

		assertThat(predicate.test(this.value1, this.value2)).isFalse();
		assertThat(predicate.test(this.value1, this.value2)).isFalse();
		assertThat(predicate.test(this.value1, this.value2)).isFalse();
		assertThat(predicate.test(this.value1, this.value2)).isTrue();
	}

	@Test
	public void or() {
		Predicate<Object> predicate1 = mock(Predicate.class);
		Predicate<Object> predicate2 = mock(Predicate.class);
		doReturn(false, false, true, true).when(predicate1).test(this.value1);
		doReturn(false, true, false, true).when(predicate2).test(this.value2);

		BiPredicate<Object, Object> predicate = BiPredicate.or(predicate1, predicate2);

		assertThat(predicate.test(this.value1, this.value2)).isFalse();
		assertThat(predicate.test(this.value1, this.value2)).isTrue();
		assertThat(predicate.test(this.value1, this.value2)).isTrue();
		assertThat(predicate.test(this.value1, this.value2)).isTrue();
	}
}
