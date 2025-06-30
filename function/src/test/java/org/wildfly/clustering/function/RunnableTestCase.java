/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * Unit test for {@link Runnable}.
 * @author Paul Ferraro
 */
public class RunnableTestCase {

	@Test
	public void andThen() {
		Runnable runner1 = mock(Runnable.class);
		Runnable runner2 = mock(Runnable.class);
		InOrder order = inOrder(runner1, runner2);
		doCallRealMethod().when(runner1).andThen(any());

		runner1.andThen(runner2).run();

		order.verify(runner1).run();
		order.verify(runner2).run();
	}

	@Test
	public void handle() {
		Runnable runner = mock(Runnable.class);
		Consumer<RuntimeException> handler = mock(Consumer.class);
		RuntimeException exception = new RuntimeException();

		doCallRealMethod().when(runner).handle(any());

		runner.handle(handler).run();

		verify(runner).run();
		verify(handler, never()).accept(any());

		doThrow(exception).when(runner).run();

		runner.handle(handler).run();

		verify(handler).accept(exception);
	}

	@Test
	public void composite() {
		Runnable runner1 = mock(Runnable.class);
		Runnable runner2 = mock(Runnable.class);
		Runnable runner3 = mock(Runnable.class);
		InOrder order = inOrder(runner1, runner2, runner3);

		Runnable.runAll(List.of(runner1, runner2, runner3)).run();

		order.verify(runner1).run();
		order.verify(runner2).run();
		order.verify(runner3).run();
	}

	@Test
	public void acceptProvided() {
		Object value = new Object();
		Consumer<Object> consumer = mock(Consumer.class);

		Runnable.accept(consumer, Supplier.of(value)).run();

		verify(consumer).accept(value);
	}
}
