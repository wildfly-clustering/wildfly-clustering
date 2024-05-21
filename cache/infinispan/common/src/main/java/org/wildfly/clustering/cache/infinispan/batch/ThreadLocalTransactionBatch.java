/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.infinispan.batch;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.BatchContext;
import org.wildfly.clustering.cache.batch.SuspendedBatch;

/**
 * {@link TransactionBatch} that associates and exposes the underlying transaction.
 * @param <E> the exception wrapper type for transaction-related exceptions
 * @author Paul Ferraro
 */
public class ThreadLocalTransactionBatch<E extends RuntimeException> implements TransactionBatch {

	private static final Batch NOOP_BATCH = Batch.factory().get();
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

	private final TransactionManager tm;
	private final Transaction tx;
	private final Logger logger;
	private final Function<Throwable, E> exceptionTransformer;
	private final AtomicInteger count = new AtomicInteger(0);

	private volatile boolean active = true;

	ThreadLocalTransactionBatch(TransactionManager tm, Transaction tx, Logger logger, Function<Throwable, E> exceptionTransformer) {
		this.tm = tm;
		this.tx = tx;
		this.logger = logger;
		this.exceptionTransformer = exceptionTransformer;
		this.logger.debugf("Started batch %s[%d]", this.tx, this.count.get());
	}

	private int getStatus() {
		try {
			return this.tx.getStatus();
		} catch (SystemException e) {
			throw this.exceptionTransformer.apply(e);
		}
	}

	@Override
	public boolean isActive() {
		int status = this.getStatus();
		return (status == Status.STATUS_ACTIVE) && this.active;
	}

	@Override
	public boolean isDiscarding() {
		int status = this.getStatus();
		return ((status == Status.STATUS_ACTIVE) && !this.active) || (status == Status.STATUS_MARKED_ROLLBACK);
	}

	@Override
	public boolean isClosed() {
		int status = this.getStatus();
		return (status != Status.STATUS_ACTIVE) && (status != Status.STATUS_MARKED_ROLLBACK);
	}

	@Override
	public SuspendedBatch suspend() {
		Batch batch = getCurrentBatch();
		if (batch == null) {
			return NOOP_BATCH.suspend();
		}
		if (batch != this) {
			// Already suspended
			return this;
		}
		try {
			Transaction suspendedTx = this.tm.suspend();
			if (suspendedTx != this.tx) {
				throw new IllegalStateException();
			}
		} catch (SystemException e) {
			throw this.exceptionTransformer.apply(e);
		}
		setCurrentBatch(null);
		return this;
	}

	@Override
	public Batch resume() {
		TransactionBatch batch = getCurrentBatch();
		if (batch == this) {
			// Already resumed
			return this;
		}
		if (batch != null) {
			batch.suspend();
		}
		if (this.isClosed()) {
			return NOOP_BATCH;
		}
		try {
			this.tm.resume(this.tx);
		} catch (SystemException | InvalidTransactionException e) {
			throw this.exceptionTransformer.apply(e);
		}
		setCurrentBatch(this);
		return this;
	}

	@Override
	public BatchContext<Batch> resumeWithContext() {
		TransactionBatch batch = getCurrentBatch();
		if (batch == this) {
			// Already resumed
			return new BatchContext<>() {
				@Override
				public Batch get() {
					return batch;
				}

				@Override
				public void close() {
				}
			};
		}
		Batch resumed = this.resume();
		return new BatchContext<>() {
			@Override
			public Batch get() {
				return resumed;
			}

			@Override
			public void close() {
				resumed.suspend();
				if (batch != null) {
					batch.resume();
				}
			}
		};
	}

	@Override
	public Transaction getTransaction() {
		return this.tx;
	}

	@Override
	public TransactionBatch interpose() {
		if (getCurrentBatch() != this) {
			throw new IllegalStateException();
		}
		int count = this.count.incrementAndGet();
		this.logger.debugf("Interposed batch %s[%d]", this.tx, count);
		return this;
	}

	@Override
	public void discard() {
		if (getCurrentBatch() != this) {
			throw new IllegalStateException();
		}
		// Allow additional cache operations prior to close, rather than call tx.setRollbackOnly()
		this.active = false;
	}

	@Override
	public void close() {
		if (getCurrentBatch() != this) {
			throw new IllegalStateException();
		}
		int count = this.count.getAndDecrement();
		if (count == 0) {
			try {
				switch (this.tx.getStatus()) {
					case Status.STATUS_ACTIVE:
						if (this.active) {
							try {
								this.logger.debugf("Committing batch %s[%d]", this.tx, count);
								this.tx.commit();
								break;
							} catch (RollbackException e) {
								throw new IllegalStateException(e);
							} catch (HeuristicMixedException | HeuristicRollbackException e) {
								throw this.exceptionTransformer.apply(e);
							}
						}
						// Otherwise fall through
					case Status.STATUS_MARKED_ROLLBACK:
						this.logger.debugf("Rolling back batch %s[%d]", this.tx, count);
						this.tx.rollback();
						break;
					default:
						this.logger.debugf("Closed batch %s[%d] with status = %d", this.tx, count, this.tx.getStatus());
				}
			} catch (SystemException e) {
				throw this.exceptionTransformer.apply(e);
			}
		} else {
			this.logger.debugf("Closed interposed batch %s[%d]", this.tx, count);
		}
	}

	@Override
	public int hashCode() {
		return this.tx.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ThreadLocalTransactionBatch)) return false;
		ThreadLocalTransactionBatch<?> batch = (ThreadLocalTransactionBatch<?>) object;
		return this.tx.equals(batch.tx);
	}

	@Override
	public String toString() {
		return String.format("%s[%d]", this.tx, this.count.get());
	}
}
