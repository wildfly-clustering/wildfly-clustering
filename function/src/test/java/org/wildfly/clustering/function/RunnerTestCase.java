/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * Unit test for {@link Runnable}.
 * @author Paul Ferraro
 */
public class RunnerTestCase {

	@Test
	public void andThen() {
		Runner runner1 = mock(Runner.class);
		Runner runner2 = mock(Runner.class);
		InOrder order = inOrder(runner1, runner2);
		doCallRealMethod().when(runner1).andThen(any());

		runner1.andThen(runner2).run();

		order.verify(runner1).run();
		order.verify(runner2).run();
	}

	@Test
	public void compose() {
		Runner runner1 = mock(Runner.class);
		Runner runner2 = mock(Runner.class);
		InOrder order = inOrder(runner1, runner2);
		doCallRealMethod().when(runner2).compose(any());

		runner2.compose(runner1).run();

		order.verify(runner1).run();
		order.verify(runner2).run();
	}

	@Test
	public void handle() {
		Runner runner = mock(Runner.class);
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
	public void throwing() {
		RuntimeException expected = new RuntimeException();
		assertThatThrownBy(Runner.throwing(Supplier.of(expected))::run).isSameAs(expected);
	}

	@Test
	public void runAll() {
		Runnable runner1 = mock(Runnable.class);
		Runnable runner2 = mock(Runnable.class);
		Runnable runner3 = mock(Runnable.class);
		InOrder order = inOrder(runner1, runner2, runner3);

		Runner.runAll(List.of(runner1, runner2, runner3)).run();

		order.verify(runner1).run();
		order.verify(runner2).run();
		order.verify(runner3).run();
	}

	@Test
	public void acceptProvided() {
		Object value = new Object();
		Consumer<Object> consumer = mock(Consumer.class);

		Runner.accept(consumer, Supplier.of(value)).run();

		verify(consumer).accept(value);
	}
}
