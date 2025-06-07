/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.wildfly.clustering.cache.CacheEntryMutator;

/**
 * A mutator decorator that mutates once per transaction.
 * @author Paul Ferraro
 */
public class TransactionalCacheEntryMutator implements CacheEntryMutator, Synchronization {
	private static final CompletionStage<Void> COMPLETED = CompletableFuture.completedStage(null);

	private final CacheEntryMutator mutator;
	private final TransactionManager tm;
	private final AtomicBoolean mutated = new AtomicBoolean(false);

	public TransactionalCacheEntryMutator(CacheEntryMutator mutator, TransactionManager tm) {
		this.mutator = mutator;
		this.tm = tm;
	}

	@Override
	public CompletionStage<Void> mutateAsync() {
		// We only need to mutate once per tx, which should defer until tx commit
		if (this.mutated.compareAndSet(false, true)) {
			try {
				// Reset mutation state on tx completion
				Transaction tx = this.tm.getTransaction();
				if (tx != null) {
					tx.registerSynchronization(this);
				}
			} catch (SystemException | RollbackException e) {
				LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
			}
			return this.mutator.mutateAsync();
		}
		return COMPLETED;
	}

	@Override
	public CacheEntryMutator withMaxIdle(Supplier<Duration> maxIdle) {
		this.mutator.withMaxIdle(maxIdle);
		return this;
	}

	@Override
	public void beforeCompletion() {
	}

	@Override
	public void afterCompletion(int status) {
		this.mutated.set(false);
	}
}
