/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import jakarta.transaction.Transaction;

import org.wildfly.clustering.cache.batch.Batch;

/**
 * @author Paul Ferraro
 */
public interface TransactionBatch extends Batch {
	/**
	 * Returns the transaction associated with this batch
	 * @return a transaction
	 */
	Transaction getTransaction();

	/**
	 * Returns an interposed batch.
	 * @return an interposed batch.
	 */
	TransactionBatch interpose();
}
