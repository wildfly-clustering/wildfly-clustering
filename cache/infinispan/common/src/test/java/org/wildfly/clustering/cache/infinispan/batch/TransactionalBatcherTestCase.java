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
import org.wildfly.clustering.cache.batch.Batcher;

/**
 * Unit test for {@link InfinispanBatcher}.
 * @author Paul Ferraro
 */
public class TransactionalBatcherTestCase {
	private final TransactionManager tm = mock(TransactionManager.class);
	private final Batcher<TransactionBatch> batcher = new TransactionalBatcher<>(this.tm, RuntimeException::new);

	@AfterEach
	public void destroy() {
		TransactionalBatcher.setCurrentBatch(null);
	}

	private static TransactionBatch mockBatch() {
		TransactionBatch batch = mock(TransactionBatch.class);
		doCallRealMethod().when(batch).isActive();
		doCallRealMethod().when(batch).isDiscarded();
		doCallRealMethod().when(batch).isClosed();
		return batch;
	}

	@Test
	public void createExistingActiveBatch() throws Exception {
		TransactionBatch existingBatch = mockBatch();

		TransactionalBatcher.setCurrentBatch(existingBatch);
		doReturn(Batch.State.ACTIVE).when(existingBatch).getState();
		doReturn(existingBatch).when(existingBatch).interpose();

		TransactionBatch result = this.batcher.createBatch();

		verify(existingBatch).interpose();
		verifyNoInteractions(this.tm);

		assertSame(existingBatch, result);
	}

	@Test
	public void createExistingClosedBatch() throws Exception {
		TransactionBatch existingBatch = mockBatch();
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		TransactionalBatcher.setCurrentBatch(existingBatch);
		doReturn(Batch.State.CLOSED).when(existingBatch).getState();
		doReturn(tx).when(this.tm).getTransaction();

		try (TransactionBatch batch = this.batcher.createBatch()) {
			verify(this.tm).begin();
			verify(tx).registerSynchronization(capturedSync.capture());

			assertSame(tx, batch.getTransaction());
			assertSame(batch, TransactionalBatcher.getCurrentBatch());
		} finally {
			capturedSync.getValue().afterCompletion(Status.STATUS_COMMITTED);
		}

		verify(tx).commit();

		assertNull(TransactionalBatcher.getCurrentBatch());
	}


	@Test
	public void createBatchClose() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(tx).when(this.tm).getTransaction();

		try (TransactionBatch batch = this.batcher.createBatch()) {
			verify(this.tm).begin();
			verify(tx).registerSynchronization(capturedSync.capture());

			assertSame(tx, batch.getTransaction());
		} finally {
			capturedSync.getValue().afterCompletion(Status.STATUS_COMMITTED);
		}

		verify(tx).commit();

		assertNull(TransactionalBatcher.getCurrentBatch());
	}

	@Test
	public void createBatchDiscard() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(tx).when(this.tm).getTransaction();

		try (TransactionBatch batch = this.batcher.createBatch()) {
			verify(this.tm).begin();
			verify(tx).registerSynchronization(capturedSync.capture());

			assertSame(tx, batch.getTransaction());

			batch.discard();
		} finally {
			capturedSync.getValue().afterCompletion(Status.STATUS_ROLLEDBACK);
		}

		verify(tx, never()).commit();
		verify(tx).rollback();

		assertNull(TransactionalBatcher.getCurrentBatch());
	}

	@Test
	public void createNestedBatchClose() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(tx).when(this.tm).getTransaction();

		try (TransactionBatch outerBatch = this.batcher.createBatch()) {
			assertSame(tx, outerBatch.getTransaction());

			verify(this.tm).suspend();
			verify(this.tm).begin();
			verify(tx).registerSynchronization(capturedSync.capture());

			try (TransactionBatch innerBatch = this.batcher.createBatch()) {
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

		assertNull(TransactionalBatcher.getCurrentBatch());
	}

	@Test
	public void createNestedBatchDiscard() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(tx).when(this.tm).getTransaction();

		try (TransactionBatch outerBatch = this.batcher.createBatch()) {
			verify(this.tm).suspend();
			verify(this.tm).begin();
			verify(tx).registerSynchronization(capturedSync.capture());

			assertSame(tx, outerBatch.getTransaction());

			doReturn(Status.STATUS_ACTIVE).when(tx).getStatus();
			doReturn(tx).when(this.tm).getTransaction();

			try (TransactionBatch innerBatch = this.batcher.createBatch()) {
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

		assertNull(TransactionalBatcher.getCurrentBatch());
	}

	@Test
	public void createOverlappingBatchClose() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(tx).when(this.tm).getTransaction();

		TransactionBatch batch = this.batcher.createBatch();

		verify(this.tm).suspend();
		verify(this.tm).begin();
		verify(tx).registerSynchronization(capturedSync.capture());

		try {
			assertSame(tx, batch.getTransaction());

			doReturn(tx).when(this.tm).getTransaction();
			doReturn(Status.STATUS_ACTIVE).when(tx).getStatus();

			try (TransactionBatch innerBatch = this.batcher.createBatch()) {
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

		assertNull(TransactionalBatcher.getCurrentBatch());
	}

	@Test
	public void createOverlappingBatchDiscard() throws Exception {
		Transaction tx = mock(Transaction.class);
		ArgumentCaptor<Synchronization> capturedSync = ArgumentCaptor.forClass(Synchronization.class);

		doReturn(tx).when(this.tm).getTransaction();

		TransactionBatch batch = this.batcher.createBatch();

		verify(this.tm).begin();
		verify(tx).registerSynchronization(capturedSync.capture());

		try {
			assertSame(tx, batch.getTransaction());

			doReturn(tx).when(this.tm).getTransaction();
			doReturn(Status.STATUS_ACTIVE).when(tx).getStatus();

			try (TransactionBatch innerBatch = this.batcher.createBatch()) {
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

		assertNull(TransactionalBatcher.getCurrentBatch());
	}

	@Test
	public void resumeNullBatch() throws Exception {
		TransactionBatch batch = mockBatch();
		TransactionalBatcher.setCurrentBatch(batch);

		try (BatchContext<TransactionBatch> context = this.batcher.resumeBatch(null)) {
			verifyNoInteractions(this.tm);
			assertNull(TransactionalBatcher.getCurrentBatch());
		}
		verifyNoInteractions(this.tm);
		assertSame(batch, TransactionalBatcher.getCurrentBatch());
	}

	@Test
	public void resumeClosedBatch() throws Exception {
		TransactionBatch existingBatch = mockBatch();
		TransactionalBatcher.setCurrentBatch(existingBatch);
		TransactionBatch batch = mockBatch();

		doReturn(Batch.State.CLOSED).when(batch).getState();

		try (BatchContext<TransactionBatch> context = this.batcher.resumeBatch(batch)) {
			verifyNoInteractions(this.tm);
			assertNull(TransactionalBatcher.getCurrentBatch());
		}
		verifyNoInteractions(this.tm);
		assertSame(existingBatch, TransactionalBatcher.getCurrentBatch());
	}

	@Test
	public void resumeNonTxBatch() throws Exception {
		TransactionBatch existingBatch = mockBatch();
		TransactionalBatcher.setCurrentBatch(existingBatch);
		TransactionBatch batch = mockBatch();

		try (BatchContext<TransactionBatch> context = this.batcher.resumeBatch(batch)) {
			verifyNoInteractions(this.tm);
			assertSame(batch, TransactionalBatcher.getCurrentBatch());
		}
		verifyNoInteractions(this.tm);
		assertSame(existingBatch, TransactionalBatcher.getCurrentBatch());
	}

	@Test
	public void resumeBatch() throws Exception {
		TransactionBatch batch = mockBatch();
		Transaction tx = mock(Transaction.class);
		TransactionalBatcher.setCurrentBatch(null);

		doReturn(tx).when(batch).getTransaction();
		doReturn(Batch.State.ACTIVE).when(batch).getState();
		doReturn(null).when(this.tm).suspend();

		try (BatchContext<TransactionBatch> context = this.batcher.resumeBatch(batch)) {
			assertSame(batch, TransactionalBatcher.getCurrentBatch());

			verify(this.tm, never()).suspend();
			verify(this.tm).resume(tx);

			doReturn(tx).when(this.tm).suspend();
		}

		verify(this.tm).suspend();
		// Nothing to resume
		verifyNoMoreInteractions(this.tm);

		assertNull(TransactionalBatcher.getCurrentBatch());
	}

	@Test
	public void resumeBatchExisting() throws Exception {
		TransactionBatch existingBatch = mockBatch();
		Transaction existingTx = mock(Transaction.class);
		TransactionalBatcher.setCurrentBatch(existingBatch);
		TransactionBatch batch = mockBatch();
		Transaction tx = mock(Transaction.class);

		doReturn(existingTx).when(existingBatch).getTransaction();
		doReturn(Batch.State.ACTIVE).when(existingBatch).getState();
		doReturn(tx).when(batch).getTransaction();
		doReturn(Batch.State.ACTIVE).when(batch).getState();
		doReturn(existingTx).when(this.tm).suspend();

		try (BatchContext<TransactionBatch> context = this.batcher.resumeBatch(batch)) {
			verify(this.tm).resume(tx);
			reset(this.tm);

			assertSame(batch, TransactionalBatcher.getCurrentBatch());

			when(this.tm.suspend()).thenReturn(tx);
		}

		verify(this.tm).resume(existingTx);

		assertSame(existingBatch, TransactionalBatcher.getCurrentBatch());
	}

	@Test
	public void suspendBatch() throws Exception {
		TransactionBatch batch = mockBatch();
		TransactionalBatcher.setCurrentBatch(batch);

		TransactionBatch result = this.batcher.suspendBatch();

		verify(this.tm).suspend();

		assertSame(batch, result);
		assertNull(TransactionalBatcher.getCurrentBatch());
	}

	@Test
	public void suspendNoBatch() throws Exception {
		TransactionalBatcher.setCurrentBatch(null);

		TransactionBatch result = this.batcher.suspendBatch();

		verify(this.tm, never()).suspend();

		assertNotNull(result);
		assertNull(result.getTransaction());
	}
}
