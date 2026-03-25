/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.context;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.function.Runner;

/**
 * @author Paul Ferraro
 */
public class ContextualThreadFactoryTestCase {

	@Test
	public void test() {
		ThreadFactory factory = mock(ThreadFactory.class);
		Thread expected = new Thread(Runner.of());
		Contextualizer contextualizer = mock(Contextualizer.class);
		ThreadFactory subject = new ContextualThreadFactory<>(factory, contextualizer);
		Runnable task = mock(Runnable.class);
		Runnable contextualTask = mock(Runnable.class);

		when(contextualizer.contextualize(task)).thenReturn(contextualTask);
		when(factory.newThread(same(contextualTask))).thenReturn(expected);

		Thread result = subject.newThread(task);

		assertThat(result).isSameAs(expected);

		verify(contextualizer).contextualize(any(Runnable.class));
	}
}
