/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.infinispan.batch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.BatchContext;
import org.wildfly.clustering.cache.batch.SuspendedBatch;

/**
 * Unit test for {@link TransactionBatch}.
 * @author Paul Ferraro
 */
public class TransactionBatchTestCase {
	private final TransactionManager tm = mock(TransactionManager.class);
	private final TransactionBatch.Factory factory = TransactionBatch.factory("test", this.tm, RuntimeException::new);

	@AfterEach
	public void destroy() {
		ThreadLocalTransactionBatch.setCurrentBatch(null);
	}

	@Test
	public void createExistingActiveBatch() throws Exception {
		TransactionBatch existingBatch = mock(TransactionBatch.class);

		ThreadLocalTransactionBatch.setCurrentBatch(existingBatch);
		doReturn(true).when(existingBatch).isActive();
		doReturn(existingBatch).when(existingBatch).interpose();

		TransactionBatch result = this.factory.get();

		verify(existingBatch).interpose();
		verifyNoInteractions(this.tm);

		assertSame(existingBatch, result);
	}

	@Test
	public void createExistingClosedBatch() throws Exception {
		TransactionBatch existingBatch = mock(TransactionBatch.class);
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		ThreadLocalTransactionBatch.setCurrentBatch(existingBatch);
		doReturn(true).when(existingBatch).isClosed();
		doReturn(tx).when(this.tm).getTransaction();

		try (TransactionBatch batch = this.factory.get()) {
			verify(this.tm).begin();
			verify(tx).registerSynchronization(capturedSync.capture());

			assertSame(tx, batch.getTransaction());
			assertSame(batch, ThreadLocalTransactionBatch.getCurrentBatch());
		} finally {
			capturedSync.getValue().afterCompletion(Status.STATUS_COMMITTED);
		}

		verify(tx).commit();

		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());
	}


	@Test
	public void createBatchClose() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(tx).when(this.tm).getTransaction();

		try (TransactionBatch batch = this.factory.get()) {
			verify(this.tm).begin();
			verify(tx).registerSynchronization(capturedSync.capture());

			assertSame(tx, batch.getTransaction());
		} finally {
			capturedSync.getValue().afterCompletion(Status.STATUS_COMMITTED);
		}

		verify(tx).commit();

		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());
	}

	@Test
	public void createBatchDiscard() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(tx).when(this.tm).getTransaction();

		try (TransactionBatch batch = this.factory.get()) {
			verify(this.tm).begin();
			verify(tx).registerSynchronization(capturedSync.capture());

			assertSame(tx, batch.getTransaction());

			batch.discard();
		} finally {
			capturedSync.getValue().afterCompletion(Status.STATUS_ROLLEDBACK);
		}

		verify(tx, never()).commit();
		verify(tx).rollback();

		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());
	}

	@Test
	public void createNestedBatchClose() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(tx).when(this.tm).getTransaction();

		try (TransactionBatch outerBatch = this.factory.get()) {
			assertSame(tx, outerBatch.getTransaction());

			verify(this.tm).suspend();
			verify(this.tm).begin();
			verify(tx).registerSynchronization(capturedSync.capture());

			try (TransactionBatch innerBatch = this.factory.get()) {
				// No new interactions
				verify(this.tm, times(1)).suspend();
				verify(this.tm, times(1)).begin();
			}

			verify(tx, never()).rollback();
			verify(tx, never()).commit();
		} finally {
			capturedSync.getValue().afterCompletion(Status.STATUS_COMMITTED);
		}

		verify(tx, never()).rollback();
		verify(tx).commit();

		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());
	}

	@Test
	public void createNestedBatchDiscard() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(tx).when(this.tm).getTransaction();

		try (TransactionBatch outerBatch = this.factory.get()) {
			verify(this.tm).suspend();
			verify(this.tm).begin();
			verify(tx).registerSynchronization(capturedSync.capture());

			assertSame(tx, outerBatch.getTransaction());

			doReturn(Status.STATUS_ACTIVE).when(tx).getStatus();
			doReturn(tx).when(this.tm).getTransaction();

			try (TransactionBatch innerBatch = this.factory.get()) {
				// No new interactions
				verify(this.tm, times(1)).suspend();
				verify(this.tm, times(1)).begin();

				innerBatch.discard();
			}

			verify(tx, never()).commit();
			verify(tx, never()).rollback();
		} finally {
			capturedSync.getValue().afterCompletion(Status.STATUS_ROLLEDBACK);
		}

		verify(tx).rollback();
		verify(tx, never()).commit();

		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());
	}

	@Test
	public void createOverlappingBatchClose() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(tx).when(this.tm).getTransaction();

		TransactionBatch batch = this.factory.get();

		verify(this.tm).suspend();
		verify(this.tm).begin();
		verify(tx).registerSynchronization(capturedSync.capture());

		try {
			assertSame(tx, batch.getTransaction());

			doReturn(tx).when(this.tm).getTransaction();
			doReturn(Status.STATUS_ACTIVE).when(tx).getStatus();

			try (TransactionBatch innerBatch = this.factory.get()) {
				// No new interactions
				verify(this.tm, times(1)).suspend();
				verify(this.tm, times(1)).begin();

				batch.close();

				verify(tx, never()).rollback();
				verify(tx, never()).commit();
			}
		} finally {
			capturedSync.getValue().afterCompletion(Status.STATUS_COMMITTED);
		}

		verify(tx, never()).rollback();
		verify(tx).commit();

		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());
	}

	@Test
	public void createOverlappingBatchDiscard() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(tx).when(this.tm).getTransaction();

		TransactionBatch batch = this.factory.get();

		verify(this.tm).begin();
		verify(tx).registerSynchronization(capturedSync.capture());

		try {
			assertSame(tx, batch.getTransaction());

			doReturn(tx).when(this.tm).getTransaction();
			doReturn(Status.STATUS_ACTIVE).when(tx).getStatus();

			try (TransactionBatch innerBatch = this.factory.get()) {
				// Verify no new interactions
				verify(this.tm, times(1)).suspend();
				verify(this.tm, times(1)).begin();

				innerBatch.discard();

				batch.close();

				verify(tx, never()).commit();
				verify(tx, never()).rollback();
			}
		} finally {
			capturedSync.getValue().afterCompletion(Status.STATUS_ROLLEDBACK);
		}

		verify(tx).rollback();
		verify(tx, never()).commit();

		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());
	}

	@Test
	public void suspendClosed() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> sync = ArgumentCaptor.captor();

		doReturn(tx).when(this.tm).getTransaction();

		TransactionBatch batch = this.factory.get();

		verify(this.tm).suspend();
		verify(this.tm).begin();
		verify(this.tm).getTransaction();
		verifyNoMoreInteractions(this.tm);
		verify(tx).registerSynchronization(sync.capture());

		assertSame(batch, ThreadLocalTransactionBatch.getCurrentBatch());
		assertSame(tx, batch.getTransaction());

		batch.close();

		verify(tx).commit();
		verifyNoMoreInteractions(this.tm);
		sync.getValue().afterCompletion(Status.STATUS_COMMITTED);

		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());

		assertNotSame(batch, batch.suspend().resume());
	}

	@Test
	public void suspendResume() throws Exception {
		ArgumentCaptor<Synchronization> sync = ArgumentCaptor.captor();
		Transaction tx = mock(Transaction.class);

		doReturn(tx).when(this.tm).getTransaction();
		doReturn(null, tx).when(this.tm).suspend();

		TransactionBatch batch = this.factory.get();

		verify(this.tm).suspend();
		verify(this.tm).begin();
		verify(this.tm).getTransaction();
		verifyNoMoreInteractions(this.tm);
		verify(tx).registerSynchronization(sync.capture());

		assertSame(batch, ThreadLocalTransactionBatch.getCurrentBatch());
		assertSame(tx, batch.getTransaction());

		SuspendedBatch suspended = batch.suspend();

		verify(this.tm, times(2)).suspend();
		verifyNoMoreInteractions(this.tm);

		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());

		assertThrows(IllegalStateException.class, batch::discard);
		assertThrows(IllegalStateException.class, batch::interpose);
		assertThrows(IllegalStateException.class, batch::close);

		Batch resumed = suspended.resume();

		verify(this.tm).resume(tx);
		verifyNoMoreInteractions(this.tm);

		assertSame(batch, resumed);
		assertSame(resumed, ThreadLocalTransactionBatch.getCurrentBatch());
	}

	@Test
	public void suspendResumeClosed() throws Exception {
		ArgumentCaptor<Synchronization> sync = ArgumentCaptor.captor();
		Transaction tx = mock(Transaction.class);

		doReturn(tx).when(this.tm).getTransaction();
		doReturn(null, tx, tx).when(this.tm).suspend();

		TransactionBatch batch = this.factory.get();

		verify(this.tm).suspend();
		verify(this.tm).begin();
		verify(this.tm).getTransaction();
		verifyNoMoreInteractions(this.tm);
		verify(tx).registerSynchronization(sync.capture());

		assertSame(batch, ThreadLocalTransactionBatch.getCurrentBatch());
		assertSame(tx, batch.getTransaction());

		SuspendedBatch suspended = batch.suspend();

		verify(this.tm, times(2)).suspend();
		verifyNoMoreInteractions(this.tm);

		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());

		try (Batch resumed = suspended.resume()) {
			verify(this.tm).resume(tx);
			verifyNoMoreInteractions(this.tm);

			assertSame(batch, resumed);
			assertSame(resumed, ThreadLocalTransactionBatch.getCurrentBatch());
		}

		verify(tx).commit();
		sync.getValue().afterCompletion(Status.STATUS_COMMITTED);
		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());
		verifyNoMoreInteractions(this.tm);

		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());
	}

	@Test
	public void suspendActiveResume() throws Exception {
		ArgumentCaptor<Synchronization> sync1 = ArgumentCaptor.captor();
		ArgumentCaptor<Synchronization> sync2 = ArgumentCaptor.captor();
		Transaction tx1 = mock(Transaction.class, "tx1");
		Transaction tx2 = mock(Transaction.class, "tx2");

		doReturn(tx1, tx2).when(this.tm).getTransaction();
		doReturn(null, tx1, null, tx2, tx1).when(this.tm).suspend();

		TransactionBatch batch1 = this.factory.get();

		verify(this.tm).suspend();
		verify(this.tm).begin();
		verify(this.tm).getTransaction();
		verifyNoMoreInteractions(this.tm);
		verify(tx1).registerSynchronization(sync1.capture());

		assertSame(batch1, ThreadLocalTransactionBatch.getCurrentBatch());
		assertSame(tx1, batch1.getTransaction());

		SuspendedBatch suspended = batch1.suspend();

		verify(this.tm, times(2)).suspend();
		verifyNoMoreInteractions(this.tm);

		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());

		try (TransactionBatch batch2 = this.factory.get()) {
			verify(this.tm, times(3)).suspend();
			verify(this.tm, times(2)).begin();
			verify(this.tm, times(2)).getTransaction();
			verifyNoMoreInteractions(this.tm);
			verify(tx2).registerSynchronization(sync2.capture());

			assertSame(batch2, ThreadLocalTransactionBatch.getCurrentBatch());
			assertSame(tx2, batch2.getTransaction());

			try (BatchContext<Batch> context = suspended.resumeWithContext()) {
				verify(this.tm, times(4)).suspend();
				verify(this.tm).resume(tx1);
				verifyNoMoreInteractions(this.tm);

				try (Batch resumed = context.get()) {
					assertSame(batch1, resumed);
					assertSame(resumed, ThreadLocalTransactionBatch.getCurrentBatch());
				}

				verify(tx1).commit();
				sync1.getValue().afterCompletion(Status.STATUS_COMMITTED);
				assertNull(ThreadLocalTransactionBatch.getCurrentBatch());
			}

			verify(this.tm).resume(tx2);
			verifyNoMoreInteractions(this.tm);

			assertSame(batch2, ThreadLocalTransactionBatch.getCurrentBatch());
		}

		sync2.getValue().afterCompletion(Status.STATUS_COMMITTED);
		assertNull(ThreadLocalTransactionBatch.getCurrentBatch());

		verifyNoMoreInteractions(this.tm);
	}
}
