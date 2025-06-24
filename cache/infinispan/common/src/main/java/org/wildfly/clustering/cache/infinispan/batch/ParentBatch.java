/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;

/**
 * A batch that can provide child batches.
 * @author Paul Ferraro
 */
public interface ParentBatch extends Batch, Supplier<ParentBatch> {
	ParentBatch CLOSED = new SimpleExtendableBatch(0L, false);

	@Override
	SuspendedParentBatch suspend();
}
