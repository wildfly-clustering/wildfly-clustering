/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import org.wildfly.clustering.cache.batch.SuspendedBatch;

/**
 * @author Paul Ferraro
 */
public interface ContextualSuspendedBatch extends SuspendedBatch, Contextual {

	@Override
	ContextualBatch resume();
}
