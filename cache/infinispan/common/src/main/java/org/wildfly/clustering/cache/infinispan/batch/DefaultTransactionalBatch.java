/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

public class DefaultTransactionalBatch extends AbstractContextualBatch implements TransactionalBatch, TransactionalSuspendedBatch, Synchronization {
	private final TransactionManager tm;
	private final Transaction tx;
	private final Status status;
	private final Function<Exception, RuntimeException> exceptionTransformer;
	private final AtomicBoolean active;

	DefaultTransactionalBatch(String name, TransactionManager tm, Function<Exception, RuntimeException> exceptionTransformer) {
		this(name, tm, begin(tm, exceptionTransformer), exceptionTransformer, new AtomicBoolean(true));
	}

	DefaultTransactionalBatch(String name, TransactionManager tm, Transaction tx, Function<Exception, RuntimeException> exceptionTransformer, AtomicBoolean active) {
		super(name, status -> {
			try {
				switch (tx.getStatus()) {
					case jakarta.transaction.Status.STATUS_ACTIVE:
						if (status.isActive()) {
							try {
								LOGGER.log(System.Logger.Level.DEBUG, "Committing batch {0}", tx);
								tx.commit();
								LOGGER.log(System.Logger.Level.DEBUG, "Committed batch {0}", tx);
								break;
							} catch (RollbackException e) {
								throw new IllegalStateException(e);
							} catch (HeuristicMixedException | HeuristicRollbackException e) {
								throw exceptionTransformer.apply(e);
							}
						}
						// Otherwise fall through
					case jakarta.transaction.Status.STATUS_MARKED_ROLLBACK:
						LOGGER.log(System.Logger.Level.DEBUG, "Rolling back batch {0}", tx);
						tx.rollback();
						LOGGER.log(System.Logger.Level.DEBUG, "Rolled back batch {0}", tx);
						break;
					default:
						LOGGER.log(System.Logger.Level.DEBUG, "Closed batch {0} with status = {2}", tx, tx.getStatus());
				}
			} catch (SystemException e) {
				throw exceptionTransformer.apply(e);
			}
		});
		this.tm = tm;
		this.tx = tx;
		this.exceptionTransformer = exceptionTransformer;
		this.active = active;
		this.status = new Status() {
			@Override
			public boolean isActive() {
				int status = this.getStatus();
				return (status == jakarta.transaction.Status.STATUS_ACTIVE) && active.get();
			}

			@Override
			public boolean isDiscarding() {
				int status = this.getStatus();
				return ((status == jakarta.transaction.Status.STATUS_ACTIVE) && !active.get()) || (status == jakarta.transaction.Status.STATUS_MARKED_ROLLBACK);
			}

			@Override
			public boolean isClosed() {
				int status = this.getStatus();
				return (status != jakarta.transaction.Status.STATUS_ACTIVE) && (status != jakarta.transaction.Status.STATUS_MARKED_ROLLBACK);
			}

			private int getStatus() {
				try {
					return tx.getStatus();
				} catch (SystemException e) {
					throw exceptionTransformer.apply(e);
				}
			}
		};
		try {
			tx.registerSynchronization(this);
		} catch (RollbackException | SystemException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Transaction begin(TransactionManager tm, Function<Exception, RuntimeException> exceptionTransformer) {
		try {
			// Ensure there is no transaction associated with the current thread
			Transaction currentTx = tm.getTransaction();
			if (currentTx != null) {
				throw new IllegalStateException(currentTx.toString());
			}
			tm.begin();
			return tm.getTransaction();
		} catch (SystemException | NotSupportedException e) {
			throw exceptionTransformer.apply(e);
		}
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
	public Status getStatus() {
		return this.status;
	}

	@Override
	public TransactionalSuspendedBatch suspend() {
		LOGGER.log(System.Logger.Level.DEBUG, "Suspending batch {0}", this);
		try {
			Transaction suspendedTx = this.tm.suspend();
			if (suspendedTx != this.tx) {
				throw new IllegalStateException(this.tx.toString());
			}
			return this;
		} catch (SystemException e) {
			throw this.exceptionTransformer.apply(e);
		}
	}

	@Override
	public TransactionalBatch resume() {
		try {
			LOGGER.log(System.Logger.Level.DEBUG, "Resuming batch {0}", this);
			this.tm.resume(this.tx);
			return this;
		} catch (SystemException | InvalidTransactionException e) {
			throw this.exceptionTransformer.apply(e);
		}
	}

	@Override
	public void discard() {
		// Allow additional cache operations prior to close, rather than call tx.setRollbackOnly()
		this.active.set(false);
	}

	@Override
	public void beforeCompletion() {
	}

	@Override
	public void afterCompletion(int status) {
		// Disassociate batch with thread on tx completion
		ThreadContextBatch.INSTANCE.accept(null);
	}

	@Override
	public int hashCode() {
		return this.getTransaction().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Transactional batch)) return false;
		return this.getTransaction().equals(batch.getTransaction());
	}

	@Override
	public String toString() {
		return Map.of("context", this.getName(), "tx", this.getTransaction()).toString();
	}
}
