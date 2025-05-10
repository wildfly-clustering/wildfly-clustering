/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Unit test for {@link Runnable}.
 * @author Paul Ferraro
 */
public class RunnableTestCase {

	@Test
	public void composite() {
		Runnable runner1 = Mockito.mock(Runnable.class);
		Runnable runner2 = Mockito.mock(Runnable.class);
		Runnable runner3 = Mockito.mock(Runnable.class);
		InOrder order = Mockito.inOrder(runner1, runner2, runner3);
		Runnable.of(List.of(runner1, runner2, runner3)).run();
		order.verify(runner1).run();
		order.verify(runner2).run();
		order.verify(runner3).run();
	}
}
