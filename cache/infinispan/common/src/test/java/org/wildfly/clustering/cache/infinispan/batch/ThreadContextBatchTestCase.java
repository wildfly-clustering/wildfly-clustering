/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.SuspendedBatch;
import org.wildfly.clustering.context.Context;

/**
 * @author Paul Ferraro
 */
public class ThreadContextBatchTestCase {

	@BeforeEach
	public void init() {
		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@AfterEach
	public void destroy() {
		ThreadContextBatch.INSTANCE.accept(null);
	}

	@Test
	public void getStatus() {
		ContextualBatch batch = mock(ContextualBatch.class);
		Batch.Status status = mock(Batch.Status.class);

		ThreadContextBatch.INSTANCE.accept(batch);

		verifyNoInteractions(batch);

		assertThat(ThreadContextBatch.INSTANCE.get()).isSameAs(batch);

		doReturn(status).when(batch).getStatus();

		assertThat(ThreadContextBatch.INSTANCE.getStatus()).isSameAs(status);
	}

	@Test
	public void resumeWhileAssociatedBatch() {
		ContextualBatch contextualBatch = mock(ContextualBatch.class);
		ContextualSuspendedBatch contextualSuspendedBatch = mock(ContextualSuspendedBatch.class);

		doReturn(contextualSuspendedBatch).when(contextualBatch).suspend();

		ThreadContextBatch.INSTANCE.accept(contextualBatch);

		SuspendedBatch suspended = ThreadContextBatch.INSTANCE.suspend();

		ContextualBatch currentBatch = mock(ContextualBatch.class);

		ThreadContextBatch.INSTANCE.accept(currentBatch);

		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(suspended::resume);

		verify(currentBatch).attach(any());
	}

	@Test
	public void resumeWithContextWhileAssociatedBatch() {
		ContextualBatch contextualBatch = mock(ContextualBatch.class);
		ContextualSuspendedBatch contextualSuspendedBatch = mock(ContextualSuspendedBatch.class);

		doReturn(contextualSuspendedBatch).when(contextualBatch).suspend();
		doReturn(contextualBatch).when(contextualSuspendedBatch).resume();

		ThreadContextBatch.INSTANCE.accept(contextualBatch);

		SuspendedBatch suspended = ThreadContextBatch.INSTANCE.suspend();

		verify(contextualBatch).suspend();

		ContextualBatch currentBatch = mock(ContextualBatch.class);
		ContextualSuspendedBatch currentSuspendedBatch = mock(ContextualSuspendedBatch.class);

		ThreadContextBatch.INSTANCE.accept(currentBatch);

		doReturn(currentSuspendedBatch).when(currentBatch).suspend();
		doReturn(currentBatch).when(currentSuspendedBatch).resume();

		try (Context<Batch> context = suspended.resumeWithContext()) {
			verify(currentBatch).suspend();
			verifyNoInteractions(currentSuspendedBatch);
			verify(contextualSuspendedBatch).resume();
			verifyNoMoreInteractions(contextualBatch);
		}

		verify(contextualBatch, times(2)).suspend();
		verifyNoMoreInteractions(contextualSuspendedBatch);
		verify(currentSuspendedBatch).resume();
		verifyNoMoreInteractions(currentBatch);
	}

	@Test
	public void disassociated() {
		// Verify behavior when no batch is associated with the current thread
		SuspendedBatch suspend = ThreadContextBatch.INSTANCE.suspend();

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		Batch resumed = suspend.resume();

		assertThat(ThreadContextBatch.INSTANCE).isSameAs(resumed);
		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		ThreadContextBatch.INSTANCE.discard();

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		ThreadContextBatch.INSTANCE.close();

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		Batch.Status status = ThreadContextBatch.INSTANCE.getStatus();
		assertThat(status).isNotNull();
		assertThat(status.isActive()).isFalse();
		assertThat(status.isDiscarding()).isFalse();
		assertThat(status.isClosed()).isTrue();

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void suspendResume() {
		ContextualBatch batch = mock(ContextualBatch.class);
		ContextualSuspendedBatch suspendedBatch = mock(ContextualSuspendedBatch.class);

		ThreadContextBatch.INSTANCE.accept(batch);

		verifyNoInteractions(batch);

		assertThat(ThreadContextBatch.INSTANCE.get()).isSameAs(batch);

		doReturn(suspendedBatch).when(batch).suspend();
		doReturn(batch).when(suspendedBatch).resume();

		SuspendedBatch suspended = ThreadContextBatch.INSTANCE.suspend();

		verify(batch, only()).suspend();
		verifyNoInteractions(suspendedBatch);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		Batch resumed = suspended.resume();

		verify(suspendedBatch, only()).resume();
		verifyNoMoreInteractions(batch);

		assertThat(ThreadContextBatch.INSTANCE).isSameAs(resumed);
		assertThat(ThreadContextBatch.INSTANCE.get()).isSameAs(batch);
	}

	@Test
	public void close() {
		ContextualBatch batch = mock(ContextualBatch.class);
		Batch.Status openStatus = mock(Batch.Status.class);
		Batch.Status closedStatus = mock(Batch.Status.class);

		ThreadContextBatch.INSTANCE.accept(batch);

		verifyNoInteractions(batch);

		assertThat(ThreadContextBatch.INSTANCE.get()).isSameAs(batch);

		doReturn(openStatus, closedStatus).when(batch).getStatus();
		doReturn(false).when(openStatus).isClosed();
		doReturn(true).when(closedStatus).isClosed();

		ThreadContextBatch.INSTANCE.close();

		verify(batch).close();
		verify(batch).getStatus();
		verifyNoMoreInteractions(batch);
		verify(openStatus).isClosed();
		verifyNoInteractions(closedStatus);

		// Verify that batch is still associated with thread if not closed
		assertThat(ThreadContextBatch.INSTANCE.get()).isSameAs(batch);

		ThreadContextBatch.INSTANCE.close();

		verify(batch, times(2)).close();
		verify(batch, times(2)).getStatus();
		verify(closedStatus).isClosed();
		verifyNoMoreInteractions(openStatus);

		// Verify that batch is no longer associated with thread if actually closed
		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void discard() {
		ContextualBatch batch = mock(ContextualBatch.class);

		ThreadContextBatch.INSTANCE.accept(batch);

		verifyNoInteractions(batch);

		assertThat(ThreadContextBatch.INSTANCE.get()).isSameAs(batch);

		ThreadContextBatch.INSTANCE.discard();

		verify(batch, only()).discard();

		assertThat(ThreadContextBatch.INSTANCE.get()).isSameAs(batch);
	}
}
