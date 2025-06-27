/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import org.wildfly.clustering.cache.batch.SuspendedBatch;

/**
 * A suspended batch that resumes a {@link ParentBatch}.
 * @author Paul Ferraro
 */
public interface SuspendedParentBatch extends SuspendedBatch {

	@Override
	ParentBatch resume();
}
