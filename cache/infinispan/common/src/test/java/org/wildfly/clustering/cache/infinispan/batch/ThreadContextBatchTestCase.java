/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.SuspendedBatch;

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
	public void isActive() {
		validate(Batch::isActive);
	}

	@Test
	public void isDiscarding() {
		validate(Batch::isDiscarding);
	}

	@Test
	public void isClosed() {
		validate(Batch::isDiscarding);
	}

	private void validate(Predicate<Batch> predicate) {
		validate(predicate, false);
		validate(predicate, true);
	}

	private void validate(Predicate<Batch> predicate, boolean expected) {
		Batch batch = mock(Batch.class);

		ThreadContextBatch.INSTANCE.accept(batch);

		verifyNoInteractions(batch);

		assertThat(ThreadContextBatch.INSTANCE.get()).isSameAs(batch);

		predicate.test(doReturn(expected).when(batch));

		boolean result = predicate.test(ThreadContextBatch.INSTANCE);

		predicate.test(verify(batch, only()));

		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void resumeWhileAssociatedBatch() {
		Batch batch1 = mock(Batch.class);

		ThreadContextBatch.INSTANCE.accept(batch1);

		SuspendedBatch suspended = ThreadContextBatch.INSTANCE.suspend();

		ThreadContextBatch.INSTANCE.accept(mock(Batch.class));

		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(suspended::resume);
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

		assertThat(ThreadContextBatch.INSTANCE.isActive()).isFalse();
		assertThat(ThreadContextBatch.INSTANCE.isDiscarding()).isFalse();
		assertThat(ThreadContextBatch.INSTANCE.isClosed()).isTrue();

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void suspendResume() {
		Batch batch = mock(Batch.class);
		SuspendedBatch suspendedBatch = mock(SuspendedBatch.class);

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
		Batch batch = mock(Batch.class);

		ThreadContextBatch.INSTANCE.accept(batch);

		verifyNoInteractions(batch);

		assertThat(ThreadContextBatch.INSTANCE.get()).isSameAs(batch);

		doReturn(false, true).when(batch).isClosed();

		ThreadContextBatch.INSTANCE.close();

		verify(batch).close();
		verify(batch).isClosed();
		verifyNoMoreInteractions(batch);

		// Verify that batch is still associated with thread if not closed
		assertThat(ThreadContextBatch.INSTANCE.get()).isSameAs(batch);

		ThreadContextBatch.INSTANCE.close();

		verify(batch, times(2)).isClosed();
		verify(batch, times(2)).close();

		// Verify that batch is no longer associated with thread if actually closed
		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void discard() {
		Batch batch = mock(Batch.class);

		ThreadContextBatch.INSTANCE.accept(batch);

		verifyNoInteractions(batch);

		assertThat(ThreadContextBatch.INSTANCE.get()).isSameAs(batch);

		ThreadContextBatch.INSTANCE.discard();

		verify(batch, only()).discard();

		assertThat(ThreadContextBatch.INSTANCE.get()).isSameAs(batch);
	}
}
