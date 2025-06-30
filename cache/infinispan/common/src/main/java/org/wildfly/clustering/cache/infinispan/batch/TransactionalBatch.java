/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import org.wildfly.clustering.cache.batch.Batch;

/**
 * An extendable transactional {@link Batch}.
 * @author Paul Ferraro
 */
interface TransactionalBatch extends ContextualBatch, Transactional {

	@Override
	TransactionalSuspendedBatch suspend();
}
