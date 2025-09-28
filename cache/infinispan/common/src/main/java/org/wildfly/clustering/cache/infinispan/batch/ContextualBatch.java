/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;

/**
 * A contextual batch.
 * @author Paul Ferraro
 */
public interface ContextualBatch extends Batch, Contextual, Supplier<ContextualBatch> {
	/** The logger for this batch */
	System.Logger LOGGER = System.getLogger(Batch.class.getName());

	@Override
	ContextualSuspendedBatch suspend();
}
