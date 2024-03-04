/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.infinispan.batch;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.BatchContext;
import org.wildfly.clustering.cache.batch.Batcher;

/**
 * A {@link Batcher} implementation based on Infinispan's BatchContainer, except that its transaction reference
 * is stored within the returned Batch object instead of a ThreadLocal.  This also allows the user to call {@link Batch#close()} from a
 * different thread than the one that created the {@link Batch}.  In this case, however, the user must first resume the batch
 * via {@link #resumeBatch(TransactionBatch)}.
 * @author Paul Ferraro
 */
public class TransactionalBatcher<E extends RuntimeException> implements Batcher<TransactionBatch> {

	private static final TransactionBatch NON_TX_BATCH = new TransactionBatch() {
		@Override
		public void close() {
			// No-op
		}

		@Override
		public void discard() {
			// No-op
		}

		@Override
		public State getState() {
			// A non-tx batch is always active
			return State.ACTIVE;
		}

		@Override
		public Transaction getTransaction() {
			return null;
		}

		@Override
		public TransactionBatch interpose() {
			return this;
		}
	};

	private static final BatchContext<TransactionBatch> NON_TX_BATCH_CONTEXT = new BatchContext<>() {
		@Override
		public TransactionBatch getBatch() {
			return NON_TX_BATCH;
		}

		@Override
		public void close() {
		}
	};

	// Used to coalesce interposed transactions
	private static final ThreadLocal<TransactionBatch> CURRENT_BATCH = new ThreadLocal<>();

	static TransactionBatch getCurrentBatch() {
		return CURRENT_BATCH.get();
	}

	static void setCurrentBatch(TransactionBatch batch) {
		if (batch != null) {
			CURRENT_BATCH.set(batch);
		} else {
			CURRENT_BATCH.remove();
		}
	}

	private static final Synchronization CURRENT_BATCH_SYNCHRONIZATION = new Synchronization() {
		@Override
		public void beforeCompletion() {
		}

		@Override
		public void afterCompletion(int status) {
			setCurrentBatch(null);
		}
	};

	private final TransactionManager tm;
	private final Function<Throwable, E> exceptionTransformer;

	public TransactionalBatcher(TransactionManager tm, Function<Throwable, E> exceptionTransformer) {
		this.tm = tm;
		this.exceptionTransformer = exceptionTransformer;
	}

	@Override
	public TransactionBatch createBatch() {
		if (this.tm == null) return NON_TX_BATCH;
		TransactionBatch batch = getCurrentBatch();
		try {
			if ((batch != null) && batch.isActive()) {
				return batch.interpose();
			}
			Transaction suspendedTx = this.tm.suspend();
			// Ensure there is no current transaction
			if ((suspendedTx != null) && (suspendedTx.getStatus() != Status.STATUS_NO_TRANSACTION)) {
				throw new IllegalStateException(suspendedTx.toString());
			}
			this.tm.begin();
			Transaction tx = this.tm.getTransaction();
			tx.registerSynchronization(CURRENT_BATCH_SYNCHRONIZATION);
			batch = new TransactionalBatch<>(tx, this.exceptionTransformer);
			setCurrentBatch(batch);
			return batch;
		} catch (RollbackException | SystemException | NotSupportedException e) {
			throw this.exceptionTransformer.apply(e);
		}
	}

	@Override
	public BatchContext<TransactionBatch> resumeBatch(TransactionBatch batch) {
		TransactionBatch suspendingBatch = getCurrentBatch();
		TransactionBatch resumingBatch = Optional.ofNullable(batch).filter(Predicate.not(Batch::isClosed)).orElse(null);
		return (suspendingBatch != resumingBatch) ? new TransactionalBatchContext(suspendingBatch, resumingBatch) : NON_TX_BATCH_CONTEXT;
	}

	@Override
	public TransactionBatch suspendBatch() {
		if (this.tm == null) return NON_TX_BATCH;
		TransactionBatch batch = getCurrentBatch();
		if (batch != null) {
			try {
				Transaction tx = this.tm.suspend();
				if (batch.getTransaction() != tx) {
					throw new IllegalStateException();
				}
			} catch (SystemException e) {
				throw this.exceptionTransformer.apply(e);
			} finally {
				setCurrentBatch(null);
			}
		}
		return Optional.ofNullable(batch).filter(Predicate.not(Batch::isClosed)).orElse(NON_TX_BATCH);
	}

	private class TransactionalBatchContext implements BatchContext<TransactionBatch> {
		private final TransactionManager tm = TransactionalBatcher.this.tm;
		private final Function<Throwable, E> exceptionTransformer = TransactionalBatcher.this.exceptionTransformer;
		private final TransactionBatch suspendedBatch;
		private final TransactionBatch resumedBatch;

		TransactionalBatchContext(TransactionBatch suspendingBatch, TransactionBatch resumingBatch) {
			Transaction suspendingTx = (suspendingBatch != null) ? suspendingBatch.getTransaction() : null;
			if (suspendingTx != null) {
				try {
					if (this.tm.suspend() != suspendingTx) {
						throw new IllegalStateException();
					}
				} catch (SystemException e) {
					throw this.exceptionTransformer.apply(e);
				}
			}
			Transaction resumingTx = (resumingBatch != null) ? resumingBatch.getTransaction() : null;
			if (resumingTx != null) {
				try {
					this.tm.resume(resumingTx);
				} catch (SystemException | InvalidTransactionException e) {
					throw this.exceptionTransformer.apply(e);
				}
			}
			this.suspendedBatch = suspendingBatch;
			this.resumedBatch = resumingBatch;
			setCurrentBatch(resumingBatch);
		}

		@Override
		public TransactionBatch getBatch() {
			return Optional.ofNullable(getCurrentBatch()).orElse(NON_TX_BATCH);
		}

		@Override
		public void close() {
			Transaction resumedTx = (this.resumedBatch != null) ? resumedBatch.getTransaction() : null;
			if (resumedTx != null) {
				try {
					if (this.tm.suspend() != resumedTx) {
						throw new IllegalStateException();
					}
				} catch (SystemException e) {
					throw this.exceptionTransformer.apply(e);
				}
			}
			Transaction suspendedTx = (this.suspendedBatch != null) ? suspendedBatch.getTransaction() : null;
			if (suspendedTx != null) {
				try {
					this.tm.resume(suspendedTx);
				} catch (SystemException | InvalidTransactionException e) {
					throw this.exceptionTransformer.apply(e);
				}
			}
			setCurrentBatch(this.suspendedBatch);
		}
	}
}
