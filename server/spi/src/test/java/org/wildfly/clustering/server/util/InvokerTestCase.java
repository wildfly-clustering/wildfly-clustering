/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.wildfly.common.function.ExceptionRunnable;
import org.wildfly.common.function.ExceptionSupplier;

/**
 * @author Paul Ferraro
 */
public class InvokerTestCase {

	@Test
	public void testSupplier() throws Exception {
		Exception[] exceptions = new Exception[3];
		for (int i = 0; i < exceptions.length; ++i) {
			exceptions[i] = new Exception();
		}
		Object expected = new Object();
		ExceptionSupplier<Object, Exception> action = mock(ExceptionSupplier.class);

		Invoker invoker = Invoker.retrying(List.of(Duration.ZERO, Duration.ofMillis(1)));

		when(action.get()).thenReturn(expected);

		Object result = invoker.invoke(action);
		assertSame(expected, result);

		when(action.get()).thenThrow(exceptions[0]).thenReturn(expected);

		result = invoker.invoke(action);
		assertSame(expected, result);

		when(action.get()).thenThrow(exceptions[0], exceptions[1]).thenReturn(expected);

		result = invoker.invoke(action);
		assertSame(expected, result);

		when(action.get()).thenThrow(exceptions).thenReturn(expected);

		try {
			result = invoker.invoke(action);
			fail("Expected exception");
		} catch (Exception e) {
			assertSame(exceptions[2], e);
		}
	}

	@Test
	public void testRunnable() throws Exception {
		Exception[] exceptions = new Exception[3];
		for (int i = 0; i < exceptions.length; ++i) {
			exceptions[i] = new Exception();
		}
		ExceptionRunnable<Exception> action = mock(ExceptionRunnable.class);

		doNothing().when(action).run();

		Invoker invoker = Invoker.retrying(List.of(Duration.ZERO, Duration.ZERO));

		invoker.invoke(action);

		doThrow(exceptions[0]).doNothing().when(action).run();

		invoker.invoke(action);

		doThrow(exceptions[0], exceptions[1]).doNothing().when(action).run();

		invoker.invoke(action);

		doThrow(exceptions).doNothing().when(action).run();

		try {
			invoker.invoke(action);
			fail("Expected exception");
		} catch (Exception e) {
			assertSame(exceptions[2], e);
		}
	}
}
