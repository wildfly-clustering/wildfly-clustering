/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.transaction;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.function.Runner;

/**
 * A factory for returning transaction contexts.
 * @author Paul Ferraro
 */
public interface TransactionContextFactory {
	/** A factory that returns empty contexts */
	TransactionContextFactory EMPTY = new TransactionContextFactory() {
		@Override
		public Context<Transaction> suspendWithContext() {
			return Context.empty();
		}

		@Override
		public Context<Transaction> resumeWithContext(Transaction tx) {
			return Context.empty();
		}
	};

	/**
	 * Returns a transaction context factory for the specified transaction manager.
	 * @param tm a transaction manager
	 * @return a transaction context factory for the specified transaction manager.
	 */
	static TransactionContextFactory of(TransactionManager tm) {
		return (tm != null) ? new TransactionContextFactory() {
			private final System.Logger logger = System.getLogger(TransactionContextFactory.class.getName());

			@Override
			public Context<Transaction> suspendWithContext() {
				try {
					Transaction tx = tm.suspend();
					return Context.of(tx, (tx != null) ? () -> {
						try {
							tm.resume(tx);
						} catch (SystemException | InvalidTransactionException e) {
							this.logger.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
						}
					} : Runner.of());
				} catch (SystemException e) {
					throw new IllegalStateException(e);
				}
			}

			@Override
			public Context<Transaction> resumeWithContext(Transaction tx) {
				if (tx == null) return Context.empty();
				try {
					Transaction currentTx = tm.getTransaction();
					if (tx == currentTx) return Context.of(tx, Runner.of());
					Transaction suspendedTx = tm.suspend();
					tm.resume(tx);
					return Context.of(tx, () -> {
						try {
							tm.suspend();
							if (suspendedTx != null) {
								tm.resume(suspendedTx);
							}
						} catch (SystemException | InvalidTransactionException e) {
							this.logger.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
						}
					});
				} catch (SystemException | InvalidTransactionException e) {
					throw new IllegalStateException(e);
				}
			}
		} : EMPTY;
	}

	/**
	 * Suspends any current transaction and returns a context that resumes on {@link Context#close()}.
	 * @return a context that resumes the suspended transaction on {@link Context#close()}.
	 */
	Context<Transaction> suspendWithContext();

	/**
	 * Resumes the specified transaction, if necessary, and returns a context that, if resumed, suspends on {@link Context#close()}.
	 * @param tx the transaction to resume
	 * @return a context that suspends the resumed transaction on {@link Context#close()}.
	 */
	Context<Transaction> resumeWithContext(Transaction tx);
}
