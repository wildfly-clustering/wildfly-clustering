/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.batch;

/**
 * A simple batch implementation that merely tracks state.
 * @author Paul Ferraro
 */
public class SimpleBatch implements Batch, SuspendedBatch {
	private final long id;
	private volatile boolean active;

	protected SimpleBatch(long id) {
		this(id, true);
		LOGGER.log(System.Logger.Level.DEBUG, "Created batch {0}", id);
	}

	protected SimpleBatch(long id, boolean active) {
		this.id = id;
		this.active = active;
	}

	@Override
	public Batch resume() {
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
		LOGGER.log(System.Logger.Level.DEBUG, "Closed batch {0}", this.id);
	}

	@Override
	public int hashCode() {
		return Long.hashCode(this.id);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof SimpleBatch)) return false;
		SimpleBatch batch = (SimpleBatch) object;
		return this.id == batch.id;
	}

	@Override
	public String toString() {
		return String.valueOf(this.id);
	}
}
