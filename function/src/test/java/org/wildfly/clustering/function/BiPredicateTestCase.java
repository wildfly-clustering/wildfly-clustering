/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.function.Predicate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link BiPredicate}.
 * @author Paul Ferraro
 */
public class BiPredicateTestCase {
	private Object value1 = new Object();
	private Object value2 = new Object();

	@Test
	public void former() {
		Predicate<Object> predicate1 = Mockito.mock(Predicate.class);
		Mockito.doReturn(false, true).when(predicate1).test(this.value1);

		BiPredicate<Object, Object> predicate = BiPredicate.former(predicate1);

		Assertions.assertThat(predicate.test(this.value1, this.value2)).isFalse();
		Assertions.assertThat(predicate.test(this.value1, this.value2)).isTrue();
	}

	@Test
	public void latter() {
		Predicate<Object> predicate2 = Mockito.mock(Predicate.class);
		Mockito.doReturn(false, true).when(predicate2).test(this.value2);

		BiPredicate<Object, Object> predicate = BiPredicate.latter(predicate2);

		Assertions.assertThat(predicate.test(this.value1, this.value2)).isFalse();
		Assertions.assertThat(predicate.test(this.value1, this.value2)).isTrue();
	}

	@Test
	public void negate() {
		BiPredicate<Object, Object> predicate = Mockito.mock(BiPredicate.class);
		Mockito.doCallRealMethod().when(predicate).negate();
		Mockito.doReturn(false, true).when(predicate).test(this.value1, this.value2);
		BiPredicate<Object, Object> negative = predicate.negate();
		Assertions.assertThat(negative.test(this.value1, this.value2)).isTrue();
		Assertions.assertThat(negative.test(this.value1, this.value2)).isFalse();
	}

	@Test
	public void and() {
		Predicate<Object> predicate1 = Mockito.mock(Predicate.class);
		Predicate<Object> predicate2 = Mockito.mock(Predicate.class);
		Mockito.doReturn(false, false, true, true).when(predicate1).test(this.value1);
		Mockito.doReturn(false, true, false, true).when(predicate2).test(this.value2);

		BiPredicate<Object, Object> predicate = BiPredicate.and(predicate1, predicate2);

		Assertions.assertThat(predicate.test(this.value1, this.value2)).isFalse();
		Assertions.assertThat(predicate.test(this.value1, this.value2)).isFalse();
		Assertions.assertThat(predicate.test(this.value1, this.value2)).isFalse();
		Assertions.assertThat(predicate.test(this.value1, this.value2)).isTrue();
	}

	@Test
	public void or() {
		Predicate<Object> predicate1 = Mockito.mock(Predicate.class);
		Predicate<Object> predicate2 = Mockito.mock(Predicate.class);
		Mockito.doReturn(false, false, true, true).when(predicate1).test(this.value1);
		Mockito.doReturn(false, true, false, true).when(predicate2).test(this.value2);

		BiPredicate<Object, Object> predicate = BiPredicate.or(predicate1, predicate2);

		Assertions.assertThat(predicate.test(this.value1, this.value2)).isFalse();
		Assertions.assertThat(predicate.test(this.value1, this.value2)).isTrue();
		Assertions.assertThat(predicate.test(this.value1, this.value2)).isTrue();
		Assertions.assertThat(predicate.test(this.value1, this.value2)).isTrue();
	}
}
