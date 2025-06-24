/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.batch;

/**
 * A suspended batch.
 * @author Paul Ferraro
 */
public interface SuspendedBatch {

	/**
	 * Resumes this batch.
	 * @return the resumed batch.
	 */
	Batch resume();

	/**
	 * Resumes this batch until {@link BatchContext#close()}.
	 * @return a resumed batch context
	 */
	default BatchContext<Batch> resumeWithContext() {
		Batch resumed = this.resume();
		return BatchContext.of(resumed, Batch::suspend);
	}
}
