/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * @author Paul Ferraro
 */
public class PredicateTestCase {

	@Test
	public void test() {
		assertThat(Predicate.always().test(new Object())).isTrue();
		assertThat(Predicate.always().test(null)).isTrue();
		assertThat(Predicate.never().test(new Object())).isFalse();
		assertThat(Predicate.never().test(null)).isFalse();
		assertThat(Predicate.of(true).test(new Object())).isTrue();
		assertThat(Predicate.of(true).test(null)).isTrue();
		assertThat(Predicate.of(false).test(new Object())).isFalse();
		assertThat(Predicate.of(false).test(null)).isFalse();
	}

	@Test
	public void negate() {
		assertThat(Predicate.always().negate().test(new Object())).isFalse();
		assertThat(Predicate.always().negate().test(null)).isFalse();
		assertThat(Predicate.never().negate().test(new Object())).isTrue();
		assertThat(Predicate.never().negate().test(null)).isTrue();
	}

	@Test
	public void compose() {
		Predicate<Object> predicate = mock(Predicate.class);
		Function<Object, Object> composer = mock(Function.class);
		Object value = new Object();
		Object mapped = new Object();

		doCallRealMethod().when(predicate).compose(ArgumentMatchers.<Function<Object, Object>>any());
		doReturn(mapped).when(composer).apply(value);
		doReturn(true).when(predicate).test(mapped);

		assertThat(predicate.compose(composer).test(value)).isTrue();

		verify(predicate, never()).test(value);
	}

	@Test
	public void handle() {
		Predicate<Object> predicate = mock(Predicate.class);
		BiPredicate<Object, RuntimeException> handler = mock(BiPredicate.class);
		doCallRealMethod().when(predicate).handle(any());

		Object goodValue = new Object();
		Object badValue = new Object();
		RuntimeException exception = new RuntimeException();

		doReturn(false).when(predicate).test(goodValue);
		doThrow(exception).when(predicate).test(badValue);
		doReturn(true).when(handler).test(badValue, exception);

		assertThat(predicate.handle(handler).test(goodValue)).isFalse();
		assertThat(predicate.handle(handler).test(badValue)).isTrue();

		verify(predicate).test(goodValue);
		verify(predicate).test(badValue);
		verify(handler).test(badValue, exception);
	}
}
