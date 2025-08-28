/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.batch;

import org.wildfly.clustering.context.Context;

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
	 * Resumes this batch until {@link Context#close()}.
	 * @return a resumed batch context
	 */
	default Context<Batch> resumeWithContext() {
		Batch batch = this.resume();
		return Context.of(batch, batch::suspend);
	}
}
