/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

/**
 * Implemented by transactional batch views.
 * @author Paul Ferraro
 */
public interface Transactional extends Contextual {
	/**
	 * Returns the associated transaction manager.
	 * @return the associated transaction manager.
	 */
	TransactionManager getTransactionManager();

	/**
	 * Returns the associated transaction.
	 * @return the associated transaction
	 */
	Transaction getTransaction();
}
