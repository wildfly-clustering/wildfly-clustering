/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

public class DefaultTransactionalBatch extends AbstractTransactional implements TransactionalBatch, Synchronization {
	private final String context;
	private final TransactionManager tm;
	private final Transaction tx;
	private final Function<Exception, RuntimeException> exceptionTransformer;
	private final AtomicInteger count = new AtomicInteger(0);

	private volatile boolean active = true;

	DefaultTransactionalBatch(String context, TransactionManager tm, Function<Exception, RuntimeException> exceptionTransformer) {
		this.context = context;
		this.tm = tm;
		this.exceptionTransformer = exceptionTransformer;
		try {
			// Ensure there is no transaction associated with the current thread
			Transaction currentTx = this.tm.getTransaction();
			if (currentTx != null) {
				throw new IllegalStateException(currentTx.toString());
			}
			this.tm.begin();
			this.tx = this.tm.getTransaction();
			// Register tx synchronization that clears thread context on tx completion
			this.tx.registerSynchronization(this);
			LOGGER.log(System.Logger.Level.DEBUG, "Created batch {0}", this.tx);
		} catch (RollbackException | SystemException | NotSupportedException e) {
			throw this.exceptionTransformer.apply(e);
		}
	}

	@Override
	public String getContext() {
		return this.context;
	}

	@Override
	public TransactionManager getTransactionManager() {
		return this.tm;
	}

	@Override
	public Transaction getTransaction() {
		return this.tx;
	}

	@Override
	public TransactionalBatch get() {
		int count = this.count.incrementAndGet();
		LOGGER.log(System.Logger.Level.DEBUG, "Created extended batch {0}[{1}]", this.tx, count);
		return this;
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
	public SuspendedParentBatch suspend() {
		LOGGER.log(System.Logger.Level.DEBUG, "Suspending batch {0}", this);
		try {
			Transaction suspendedTx = this.tm.suspend();
			if (suspendedTx != this.tx) {
				throw new IllegalStateException();
			}
			return new DefaultSuspendedTransactionBatch(this, this.exceptionTransformer);
		} catch (SystemException e) {
			throw this.exceptionTransformer.apply(e);
		}
	}

	@Override
	public void discard() {
		// Allow additional cache operations prior to close, rather than call tx.setRollbackOnly()
		this.active = false;
	}

	@Override
	public void close() {
		int count = this.count.getAndDecrement();
		if (count == 0) {
			try {
				switch (this.tx.getStatus()) {
					case Status.STATUS_ACTIVE:
						if (this.active) {
							try {
								LOGGER.log(System.Logger.Level.DEBUG, "Committing batch {0}", this.tx);
								this.tx.commit();
								LOGGER.log(System.Logger.Level.DEBUG, "Committed batch {0}", this.tx);
								break;
							} catch (RollbackException e) {
								throw new IllegalStateException(e);
							} catch (HeuristicMixedException | HeuristicRollbackException e) {
								throw this.exceptionTransformer.apply(e);
							}
						}
						// Otherwise fall through
					case Status.STATUS_MARKED_ROLLBACK:
						LOGGER.log(System.Logger.Level.DEBUG, "Rolling back batch {0}", this.tx);
						this.tx.rollback();
						LOGGER.log(System.Logger.Level.DEBUG, "Rolled back batch {0}", this.tx);
						break;
					default:
						LOGGER.log(System.Logger.Level.DEBUG, "Closed batch {0} with status = {2}", this.tx, this.tx.getStatus());
				}
			} catch (SystemException e) {
				throw this.exceptionTransformer.apply(e);
			}
		} else {
			LOGGER.log(System.Logger.Level.DEBUG, "Closed extended batch {0}[{1}]", this.tx, count);
		}
	}

	@Override
	public void beforeCompletion() {
	}

	@Override
	public void afterCompletion(int status) {
		// Disassociate batch with thread on tx completion
		ThreadContextBatch.INSTANCE.accept(null);
	}
}
