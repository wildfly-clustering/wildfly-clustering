/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.transaction;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

/**
 * A transaction manager decorator whose transactions register synchronizations into a {@link CompositeSynchronization}.
 * @author Paul Ferraro
 */
public class CompositeSynchronizationTransactionManager implements TransactionManager {

	private final TransactionManager tm;

	/**
	 * Decorates the specified transaaction manager.
	 * @param tm the transaction manager to decorate
	 */
	public CompositeSynchronizationTransactionManager(TransactionManager tm) {
		this.tm = tm;
	}

	@Override
	public void begin() throws NotSupportedException, SystemException {
		this.tm.begin();
		Transaction tx = this.tm.suspend();
		try {
			// Re-associate decorated transaction
			this.tm.resume(new CompositeSynchronizationTransaction(tx));
		} catch (InvalidTransactionException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
		this.tm.commit();
	}

	@Override
	public int getStatus() throws SystemException {
		return this.tm.getStatus();
	}

	@Override
	public Transaction getTransaction() throws SystemException {
		return this.tm.getTransaction();
	}

	@Override
	public void resume(Transaction tx) throws InvalidTransactionException, SystemException {
		this.tm.resume(tx);
	}

	@Override
	public void rollback() throws SystemException {
		this.tm.rollback();
	}

	@Override
	public void setRollbackOnly() throws SystemException {
		this.tm.setRollbackOnly();
	}

	@Override
	public void setTransactionTimeout(int seconds) throws SystemException {
		this.tm.setTransactionTimeout(seconds);
	}

	@Override
	public Transaction suspend() throws SystemException {
		return this.tm.suspend();
	}
}
