/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.batch;

/**
 * A simple batch implementation that merely tracks state.
 * @author Paul Ferraro
 */
class SimpleBatch implements Batch, BatchContext<Batch>, SuspendedBatch {
	private volatile boolean active;

	SimpleBatch(boolean active) {
		this.active = active;
	}

	@Override
	public Batch get() {
		return this;
	}

	@Override
	public Batch resume() {
		return this;
	}

	@Override
	public BatchContext<Batch> resumeWithContext() {
		return this;
	}

	@Override
	public SuspendedBatch suspend() {
		return this;
	}

	@Override
	public void discard() {
		// Do nothing
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public boolean isDiscarding() {
		return false;
	}

	@Override
	public boolean isClosed() {
		return !this.active;
	}

	@Override
	public void close() {
		this.active = false;
	}
}
