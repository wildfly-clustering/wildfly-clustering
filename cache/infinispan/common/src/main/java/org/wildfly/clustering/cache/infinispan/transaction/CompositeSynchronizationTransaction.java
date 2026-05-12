/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.transaction;

import java.util.Deque;
import java.util.LinkedList;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import org.infinispan.commons.tx.TransactionImpl;

/**
 * A transaction decorator that registers synchronizations into a {@link CompositeSynchronization}.
 * @author Paul Ferraro
 */
class CompositeSynchronizationTransaction implements IdentifiedTransaction {
	private final Transaction tx;
	private final Deque<Synchronization> synchronizations = new LinkedList<>();

	CompositeSynchronizationTransaction(Transaction tx) {
		this.tx = tx;
	}

	@Override
	public Xid getId() {
		return (this.tx instanceof TransactionImpl tx) ? tx.getXid() : null;
	}

	@Override
	public int getStatus() throws SystemException {
		return this.tx.getStatus();
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
		this.tx.commit();
	}

	@Override
	public void rollback() throws SystemException {
		this.tx.rollback();
	}

	@Override
	public void setRollbackOnly() throws SystemException {
		this.tx.setRollbackOnly();
	}

	@Override
	public boolean enlistResource(XAResource resource) throws RollbackException, SystemException {
		return this.tx.enlistResource(resource);
	}

	@Override
	public boolean delistResource(XAResource resource, int flag) throws SystemException {
		return this.tx.delistResource(resource, flag);
	}

	@Override
	public void registerSynchronization(Synchronization synchronization) throws RollbackException, SystemException {
		if (this.synchronizations.isEmpty()) {
			// Register singleton synchronization
			this.tx.registerSynchronization(new CompositeSynchronization(this.synchronizations));
		}
		this.synchronizations.add(synchronization);
	}
}
