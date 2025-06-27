/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import java.util.function.Function;

import jakarta.transaction.TransactionManager;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;

/**
 * @author Paul Ferraro
 */
public class TransactionalBatchFactory implements Supplier<Batch> {
	private final String name;
	private final TransactionManager tm;
	private final Function<Exception, RuntimeException> exceptionTransformer;

	public TransactionalBatchFactory(String name, TransactionManager tm, Function<Exception, RuntimeException> exceptionTransformer) {
		this.name = name;
		this.tm = tm;
		this.exceptionTransformer = exceptionTransformer;
	}

	@Override
	public Batch get() {
		TransactionalBatch batch = ThreadContextBatch.INSTANCE.get(TransactionalBatch.class);
		// If there is already an active batch associated with this thread, create a nested batch, otherwise, create a new transactional batch
		ThreadContextBatch.INSTANCE.accept((batch != null) ? batch.get() : new DefaultTransactionalBatch(this.name, this.tm, this.exceptionTransformer));
		return ThreadContextBatch.INSTANCE;
	}
}
