/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote.transaction;

import jakarta.transaction.TransactionManager;

import org.infinispan.client.hotrod.transaction.manager.RemoteTransactionManager;
import org.infinispan.commons.tx.lookup.TransactionManagerLookup;
import org.wildfly.clustering.cache.infinispan.transaction.CompositeSynchronizationTransactionManager;

/**
 * Returns a remote transaction manager.
 * @author Paul Ferraro
 */
public enum RemoteTransactionManagerLookup implements TransactionManagerLookup {
	/** The singleton instance */
	INSTANCE;

	@Override
	public TransactionManager getTransactionManager() {
		return new CompositeSynchronizationTransactionManager(RemoteTransactionManager.getInstance());
	}
}
