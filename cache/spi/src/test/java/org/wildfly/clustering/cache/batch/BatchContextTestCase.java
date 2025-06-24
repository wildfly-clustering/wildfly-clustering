/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.batch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

/**
 * Verifies behavior of default batch context methods.
 * @author Paul Ferraro
 */
public class BatchContextTestCase {

	@Test
	public void suspendWithContext() {
		Batch batch = mock(Batch.class);
		SuspendedBatch suspended = mock(SuspendedBatch.class);

		doReturn(suspended).when(batch).suspend();
		doReturn(batch).when(suspended).resume();
		doCallRealMethod().when(batch).suspendWithContext();

		try (BatchContext<SuspendedBatch> context = batch.suspendWithContext()) {
			verify(batch).suspendWithContext();
			verify(batch).suspend();
			verifyNoMoreInteractions(batch);
			verifyNoInteractions(suspended);

			assertThat(context.get()).isSameAs(suspended);
		}

		verify(suspended, only()).resume();
		verifyNoMoreInteractions(batch);
	}

	@Test
	public void resumeWithContext() {
		Batch batch = mock(Batch.class);
		SuspendedBatch suspended = mock(SuspendedBatch.class);

		doReturn(suspended).when(batch).suspend();
		doReturn(batch).when(suspended).resume();
		doCallRealMethod().when(suspended).resumeWithContext();

		try (BatchContext<Batch> context = suspended.resumeWithContext()) {
			verify(suspended).resumeWithContext();
			verify(suspended).resume();
			verifyNoMoreInteractions(suspended);
			verifyNoInteractions(batch);

			assertThat(context.get()).isSameAs(batch);
		}

		verify(batch, only()).suspend();
		verifyNoMoreInteractions(suspended);
	}
}
