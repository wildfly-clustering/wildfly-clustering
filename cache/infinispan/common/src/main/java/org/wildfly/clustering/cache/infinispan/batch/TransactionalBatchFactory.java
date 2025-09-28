/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import java.util.function.Function;

import jakarta.transaction.TransactionManager;

/**
 * A factory for creating thread context batches referencing transactional batches.
 * @author Paul Ferraro
 */
public class TransactionalBatchFactory extends ThreadContextBatchFactory {

	/**
	 * Creates a transactional batch factory.
	 * @param name the name of this context
	 * @param tm a transaaction manager
	 * @param exceptionTransformer a runtime exception wrapper for transaction exceptions
	 */
	public TransactionalBatchFactory(String name, TransactionManager tm, Function<Exception, RuntimeException> exceptionTransformer) {
		super(() -> new DefaultTransactionalBatch(name, tm, exceptionTransformer));
	}
}
