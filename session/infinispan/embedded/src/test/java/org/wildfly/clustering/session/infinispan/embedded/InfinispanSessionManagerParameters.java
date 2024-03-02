/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.transaction.TransactionMode;
import org.wildfly.clustering.session.cache.SessionManagerParameters;

/**
 * @author Paul Ferraro
 */
public interface InfinispanSessionManagerParameters extends SessionManagerParameters {
	CacheMode getCacheMode();
	TransactionMode getTransactionMode();
}
