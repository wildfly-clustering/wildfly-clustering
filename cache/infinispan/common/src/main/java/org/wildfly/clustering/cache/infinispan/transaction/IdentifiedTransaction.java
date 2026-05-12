/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.transaction;

import javax.transaction.xa.Xid;

import jakarta.transaction.Transaction;

/**
 * A transaction that exposes its {@link Xid}.
 * @author Paul Ferraro
 */
public interface IdentifiedTransaction extends Transaction {
	/**
	 * Returns the globally unique identifier of this transaction.
	 * @return the globally unique identifier of this transaction.
	 */
	Xid getId();
}
