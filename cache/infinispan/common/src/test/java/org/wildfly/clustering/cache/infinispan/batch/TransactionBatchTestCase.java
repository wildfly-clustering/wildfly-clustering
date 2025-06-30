/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.infinispan.batch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.SuspendedBatch;
import org.wildfly.clustering.function.Supplier;

/**
 * Unit test for a {@link TransactionalBatch}.
 * @author Paul Ferraro
 */
public class TransactionBatchTestCase {
	private final TransactionManager tm = mock(TransactionManager.class);
	private final Supplier<Batch> factory = new TransactionalBatchFactory("test", this.tm, RuntimeException::new);

	@AfterEach
	public void destroy() {
		// Reset thread context
		ThreadContextBatch.INSTANCE.accept(null);
	}

	@BeforeEach
	public void init() {
		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void nestedBatch() throws Exception {
		TransactionalBatch existingBatch = mock(TransactionalBatch.class);

		ThreadContextBatch.INSTANCE.accept(existingBatch);

		Batch result = this.factory.get();

		verify(existingBatch, only()).get();
		verifyNoInteractions(this.tm);

		assertThat(result).isSameAs(ThreadContextBatch.INSTANCE);
	}

	@Test
	public void illegalCurrentTransaction() throws Exception {
		Transaction existingTx = mock(Transaction.class);
		doReturn(existingTx).when(this.tm).getTransaction();

		Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(this.factory::get);

		verify(this.tm, only()).getTransaction();
	}

	@Test
	public void close() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(null, tx).when(this.tm).getTransaction();

		try (Batch batch = this.factory.get()) {
			verify(this.tm).begin();
			verify(this.tm, times(2)).getTransaction();
			verifyNoMoreInteractions(this.tm);
			verify(tx, only()).registerSynchronization(capturedSync.capture());

			assertThat(batch).isSameAs(ThreadContextBatch.INSTANCE);
			assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

			doReturn(Status.STATUS_ACTIVE, Status.STATUS_ACTIVE, Status.STATUS_COMMITTED).when(tx).getStatus();
		} finally {
			if (!capturedSync.getAllValues().isEmpty()) {
				capturedSync.getValue().afterCompletion(Status.STATUS_COMMITTED);
			}
		}

		verify(tx, times(3)).getStatus();
		verify(tx).commit();
		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void discard() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(null, tx).when(this.tm).getTransaction();

		try (Batch batch = this.factory.get()) {
			verify(this.tm).begin();
			verify(this.tm, times(2)).getTransaction();
			verifyNoMoreInteractions(this.tm);
			verify(tx, only()).registerSynchronization(capturedSync.capture());

			assertThat(batch).isSameAs(ThreadContextBatch.INSTANCE);
			assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

			batch.discard();

			verifyNoMoreInteractions(tx);

			doReturn(Status.STATUS_ACTIVE, Status.STATUS_ACTIVE, Status.STATUS_ROLLEDBACK).when(tx).getStatus();
		} finally {
			if (!capturedSync.getAllValues().isEmpty()) {
				capturedSync.getValue().afterCompletion(Status.STATUS_ROLLEDBACK);
			}
		}

		verify(tx, times(3)).getStatus();
		verify(tx).rollback();
		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void closeNested() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(null, tx).when(this.tm).getTransaction();

		try (Batch outerBatch = this.factory.get()) {
			verify(this.tm).begin();
			verify(this.tm, times(2)).getTransaction();
			verifyNoMoreInteractions(this.tm);
			verify(tx, only()).registerSynchronization(capturedSync.capture());

			assertThat(ThreadContextBatch.INSTANCE).isSameAs(outerBatch);
			assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

			doReturn(Status.STATUS_ACTIVE).when(tx).getStatus();

			try (Batch innerBatch = this.factory.get()) {
				// No new interactions
				verifyNoMoreInteractions(this.tm);
				verify(tx).getStatus();
				verifyNoMoreInteractions(tx);

				assertThat(ThreadContextBatch.INSTANCE).isSameAs(innerBatch);
				assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);
			}

			verifyNoMoreInteractions(this.tm);
			verify(tx, times(2)).getStatus();
			verifyNoMoreInteractions(tx);

			doReturn(Status.STATUS_ACTIVE, Status.STATUS_ACTIVE, Status.STATUS_COMMITTED).when(tx).getStatus();
		} finally {
			if (!capturedSync.getAllValues().isEmpty()) {
				capturedSync.getValue().afterCompletion(Status.STATUS_COMMITTED);
			}
		}

		verify(tx, times(5)).getStatus();
		verify(tx).commit();
		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void discardNestedInner() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(null, tx).when(this.tm).getTransaction();

		try (Batch outerBatch = this.factory.get()) {
			verify(this.tm, times(2)).getTransaction();
			verify(this.tm).begin();
			verifyNoMoreInteractions(this.tm);
			verify(tx, only()).registerSynchronization(capturedSync.capture());

			assertThat(ThreadContextBatch.INSTANCE).isSameAs(outerBatch);
			assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

			doReturn(Status.STATUS_ACTIVE).when(tx).getStatus();

			try (Batch innerBatch = this.factory.get()) {
				// No new interactions
				verifyNoMoreInteractions(this.tm);
				verify(tx).getStatus();
				verifyNoMoreInteractions(tx);

				assertThat(ThreadContextBatch.INSTANCE).isSameAs(innerBatch);
				assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

				innerBatch.discard();

				verifyNoMoreInteractions(this.tm);
				verifyNoMoreInteractions(tx);
			}

			verifyNoMoreInteractions(this.tm);
			verify(tx, times(2)).getStatus();
			verifyNoMoreInteractions(tx);

			doReturn(Status.STATUS_ACTIVE, Status.STATUS_ACTIVE, Status.STATUS_ROLLEDBACK).when(tx).getStatus();
		} finally {
			if (!capturedSync.getAllValues().isEmpty()) {
				capturedSync.getValue().afterCompletion(Status.STATUS_ROLLEDBACK);
			}
		}

		verify(tx, times(5)).getStatus();
		verify(tx).rollback();
		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void discardNestedOuter() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(null, tx).when(this.tm).getTransaction();

		try (Batch outerBatch = this.factory.get()) {
			verify(this.tm, times(2)).getTransaction();
			verify(this.tm).begin();
			verifyNoMoreInteractions(this.tm);
			verify(tx, only()).registerSynchronization(capturedSync.capture());

			assertThat(ThreadContextBatch.INSTANCE).isSameAs(outerBatch);
			assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

			outerBatch.discard();

			verifyNoMoreInteractions(this.tm);
			verifyNoMoreInteractions(tx);

			doReturn(Status.STATUS_ACTIVE).when(tx).getStatus();

			try (Batch innerBatch = this.factory.get()) {
				// No new interactions
				verifyNoMoreInteractions(this.tm);
				verify(tx).getStatus();
				verifyNoMoreInteractions(tx);

				assertThat(ThreadContextBatch.INSTANCE).isSameAs(innerBatch);
				assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);
			}

			verifyNoMoreInteractions(this.tm);
			verify(tx, times(2)).getStatus();
			verifyNoMoreInteractions(tx);

			doReturn(Status.STATUS_ACTIVE, Status.STATUS_ACTIVE, Status.STATUS_ROLLEDBACK).when(tx).getStatus();
		} finally {
			if (!capturedSync.getAllValues().isEmpty()) {
				capturedSync.getValue().afterCompletion(Status.STATUS_ROLLEDBACK);
			}
		}

		verify(tx, times(5)).getStatus();
		verify(tx).rollback();
		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void closeOverlapping() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(null, tx).when(this.tm).getTransaction();

		Batch batch1 = this.factory.get();

		try {
			assertThat(ThreadContextBatch.INSTANCE).isSameAs(batch1);
			assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

			verify(this.tm, times(2)).getTransaction();
			verify(this.tm).begin();
			verifyNoMoreInteractions(this.tm);
			verify(tx, only()).registerSynchronization(capturedSync.capture());
			verifyNoMoreInteractions(tx);

			doReturn(Status.STATUS_ACTIVE).when(tx).getStatus();

			try (Batch batch2 = this.factory.get()) {
				// No new interactions
				verifyNoMoreInteractions(this.tm);
				verify(tx).getStatus();
				verifyNoMoreInteractions(tx);

				assertThat(ThreadContextBatch.INSTANCE).isSameAs(batch2);
				assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

				batch1.close();

				verify(tx, times(2)).getStatus();
				verifyNoMoreInteractions(tx);
				verifyNoMoreInteractions(this.tm);
			}

			doReturn(Status.STATUS_ACTIVE, Status.STATUS_COMMITTED).when(tx).getStatus();
		} finally {
			if (!capturedSync.getAllValues().isEmpty()) {
				capturedSync.getValue().afterCompletion(Status.STATUS_COMMITTED);
			}
		}

		verify(tx, times(5)).getStatus();
		verify(tx).commit();
		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void discardOverlapping() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(null, tx).when(this.tm).getTransaction();

		Batch batch1 = this.factory.get();

		try {
			assertThat(ThreadContextBatch.INSTANCE).isSameAs(batch1);
			assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

			verify(this.tm, times(2)).getTransaction();
			verify(this.tm).begin();
			verifyNoMoreInteractions(this.tm);
			verify(tx, only()).registerSynchronization(capturedSync.capture());

			doReturn(Status.STATUS_ACTIVE).when(tx).getStatus();

			try (Batch batch2 = this.factory.get()) {
				// No new interactions
				verifyNoMoreInteractions(this.tm);
				verify(tx).getStatus();
				verifyNoMoreInteractions(tx);

				assertThat(ThreadContextBatch.INSTANCE).isSameAs(batch2);
				assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

				batch2.discard();

				verifyNoMoreInteractions(this.tm);
				verifyNoMoreInteractions(tx);

				batch1.close();

				verify(tx, times(2)).getStatus();
				verifyNoMoreInteractions(tx);
				verifyNoMoreInteractions(this.tm);
			}

			doReturn(Status.STATUS_ACTIVE, Status.STATUS_ACTIVE, Status.STATUS_ROLLEDBACK).when(tx).getStatus();
		} finally {
			if (!capturedSync.getAllValues().isEmpty()) {
				capturedSync.getValue().afterCompletion(Status.STATUS_ROLLEDBACK);
			}
		}

		verify(tx, times(5)).getStatus();
		verify(tx).rollback();
		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void suspendClosed() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.captor();

		doReturn(null, tx).when(this.tm).getTransaction();

		Batch batch = this.factory.get();

		verify(this.tm, times(2)).getTransaction();
		verify(this.tm).begin();
		verifyNoMoreInteractions(this.tm);
		verify(tx, only()).registerSynchronization(capturedSync.capture());

		assertThat(ThreadContextBatch.INSTANCE).isSameAs(batch);
		assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

		doReturn(Status.STATUS_ACTIVE, Status.STATUS_ACTIVE, Status.STATUS_COMMITTED).when(tx).getStatus();

		try {
			batch.close();
		} finally {
			if (!capturedSync.getAllValues().isEmpty()) {
				capturedSync.getValue().afterCompletion(Status.STATUS_COMMITTED);
			}
		}

		verify(tx, times(3)).getStatus();
		verify(tx).commit();
		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		this.validateDisassociated(tx);
	}

	private void validateDisassociated(Transaction tx) {
		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		// Verify behavior when no batch is associated with the current thread
		SuspendedBatch suspend = ThreadContextBatch.INSTANCE.suspend();

		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		Batch resumed = suspend.resume();

		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE).isSameAs(resumed);
		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		ThreadContextBatch.INSTANCE.discard();

		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		ThreadContextBatch.INSTANCE.close();

		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		Batch.Status status = ThreadContextBatch.INSTANCE.getStatus();
		assertThat(status).isNotNull();
		assertThat(status.isActive()).isFalse();
		assertThat(status.isDiscarding()).isFalse();
		assertThat(status.isClosed()).isTrue();

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}

	@Test
	public void suspendResume() throws Exception {
		ArgumentCaptor<Synchronization> sync = ArgumentCaptor.captor();
		Transaction tx = mock(Transaction.class);

		doReturn(null, tx).when(this.tm).getTransaction();

		Batch batch = this.factory.get();

		verify(this.tm, times(2)).getTransaction();
		verify(this.tm).begin();
		verifyNoMoreInteractions(this.tm);
		verify(tx, only()).registerSynchronization(sync.capture());

		assertThat(ThreadContextBatch.INSTANCE).isSameAs(batch);
		assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

		doReturn(tx).when(this.tm).suspend();

		SuspendedBatch suspended = batch.suspend();

		verify(this.tm).suspend();
		verifyNoMoreInteractions(this.tm);
		verifyNoMoreInteractions(tx);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		this.validateDisassociated(tx);

		Batch resumed = suspended.resume();

		verify(this.tm).resume(tx);
		verifyNoMoreInteractions(this.tm);
		verifyNoMoreInteractions(tx);

		assertThat(ThreadContextBatch.INSTANCE).isSameAs(resumed);
	}

	@Test
	public void suspendResumeClosed() throws Exception {
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.captor();
		Transaction tx = mock(Transaction.class);

		doReturn(null, tx).when(this.tm).getTransaction();

		Batch batch = this.factory.get();

		verify(this.tm, times(2)).getTransaction();
		verify(this.tm).begin();
		verifyNoMoreInteractions(this.tm);
		verify(tx, only()).registerSynchronization(capturedSync.capture());

		assertThat(ThreadContextBatch.INSTANCE).isSameAs(batch);
		assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

		doReturn(tx).when(this.tm).suspend();

		SuspendedBatch suspended = batch.suspend();

		verify(this.tm).suspend();
		verifyNoMoreInteractions(this.tm);
		verifyNoMoreInteractions(tx);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();

		try (Batch resumed = suspended.resume()) {
			verify(this.tm).resume(tx);
			verifyNoMoreInteractions(this.tm);

			assertThat(ThreadContextBatch.INSTANCE).isSameAs(resumed);
			assertThat(TransactionalBatch.class.cast(ThreadContextBatch.INSTANCE.get()).getTransaction()).isSameAs(tx);

			doReturn(Status.STATUS_ACTIVE, Status.STATUS_ACTIVE, Status.STATUS_COMMITTED).when(tx).getStatus();
		} finally {
			if (!capturedSync.getAllValues().isEmpty()) {
				capturedSync.getValue().afterCompletion(Status.STATUS_COMMITTED);
			}
		}

		verify(tx, times(3)).getStatus();
		verify(tx).commit();
		verifyNoMoreInteractions(tx);
		verifyNoMoreInteractions(this.tm);

		assertThat(ThreadContextBatch.INSTANCE.get()).isNull();
	}
}
