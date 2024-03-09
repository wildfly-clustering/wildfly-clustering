/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.infinispan.batch;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import org.jboss.logging.Logger;

/**
 * Abstract {@link TransactionBatch} that associates and exposes the underlying transaction.
 * @author Paul Ferraro
 */
public class TransactionalBatch<E extends RuntimeException> implements TransactionBatch {
	private final Logger logger;
	private final Function<Throwable, E> exceptionTransformer;
	private final Transaction tx;
	private final AtomicInteger count = new AtomicInteger(0);

	private volatile boolean active = true;

	public TransactionalBatch(Transaction tx, Logger logger, Function<Throwable, E> exceptionTransformer) {
		this.tx = tx;
		this.logger = logger;
		this.exceptionTransformer = exceptionTransformer;
		this.logger.debugf("Started batch %s[%d]", this.tx, this.count.get());
	}

	@Override
	public Transaction getTransaction() {
		return this.tx;
	}

	@Override
	public TransactionBatch interpose() {
		int count = this.count.incrementAndGet();
		this.logger.debugf("Interposed batch %s[%d]", this.tx, count);
		return this;
	}

	@Override
	public void discard() {
		// Allow additional cache operations prior to close, rather than call tx.setRollbackOnly()
		this.active = false;
	}

	@Override
	public State getState() {
		try {
			switch (this.tx.getStatus()) {
				case Status.STATUS_ACTIVE: {
					if (this.active) {
						return State.ACTIVE;
					}
					// Otherwise fall through
				}
				case Status.STATUS_MARKED_ROLLBACK: {
					return State.DISCARDED;
				}
				default: {
					return State.CLOSED;
				}
			}
		} catch (SystemException e) {
			throw this.exceptionTransformer.apply(e);
		}
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
		if (!(object instanceof TransactionalBatch)) return false;
		TransactionalBatch<?> batch = (TransactionalBatch<?>) object;
		return this.tx.equals(batch.tx);
	}

	@Override
	public String toString() {
		return String.format("%s[%d]", this.tx, this.count.get());
	}
}
