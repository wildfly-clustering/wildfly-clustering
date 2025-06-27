/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import java.util.function.Function;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.wildfly.clustering.cache.batch.Batch;

public class DefaultSuspendedTransactionBatch extends AbstractTransactional implements TransactionalSuspendedBatch {
	private final TransactionalBatch batch;
	private final Function<Exception, RuntimeException> exceptionTransformer;

	DefaultSuspendedTransactionBatch(TransactionalBatch batch, Function<Exception, RuntimeException> exceptionTransformer) {
		this.batch = batch;
		this.exceptionTransformer = exceptionTransformer;
	}

	@Override
	public String getContext() {
		return this.batch.getContext();
	}

	@Override
	public TransactionManager getTransactionManager() {
		return this.batch.getTransactionManager();
	}

	@Override
	public Transaction getTransaction() {
		return this.batch.getTransaction();
	}

	@Override
	public ParentBatch resume() {
		try {
			Batch.LOGGER.log(System.Logger.Level.DEBUG, "Resuming batch {0}", this.batch);
			this.batch.getTransactionManager().resume(this.batch.getTransaction());
			return this.batch;
		} catch (SystemException | InvalidTransactionException e) {
			throw this.exceptionTransformer.apply(e);
		}
	}
}
