/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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
		doCallRealMethod().when(runner1).thenRun(any());

		runner1.thenRun(runner2).run();

		order.verify(runner1).run();
		order.verify(runner2).run();
	}

	@Test
	public void compose() {
		Runner runner1 = mock(Runner.class);
		Runner runner2 = mock(Runner.class);
		InOrder order = inOrder(runner1, runner2);
		doCallRealMethod().when(runner2).compose(ArgumentMatchers.<Runnable>any());

		runner2.compose(runner1).run();

		order.verify(runner1).run();
		order.verify(runner2).run();
	}

	@Test
	public void runAll() {
		Runnable runner1 = mock(Runnable.class);
		Runnable runner2 = mock(Runnable.class);
		Runnable runner3 = mock(Runnable.class);
		InOrder order = inOrder(runner1, runner2, runner3);

		Runner.of(List.of(runner1, runner2, runner3)).run();

		order.verify(runner1).run();
		order.verify(runner2).run();
		order.verify(runner3).run();
	}

	@Test
	public void acceptProvided() {
		Object value = new Object();
		Consumer<Object> consumer = mock(Consumer.class);

		Runner.of(Supplier.of(value), consumer).run();

		verify(consumer).accept(value);
	}
}
