/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple extendable batch with no suspend/resume behavior.
 * @author Paul Ferraro
 */
public class SimpleContextualBatch extends AbstractContextualBatch implements ContextualSuspendedBatch {
	private final long id;
	private final Status status;

	/**
	 * Creates a contextual batch.
	 * @param name the name of this batch context
	 * @param id the usage count of this contextual batch
	 */
	SimpleContextualBatch(String name, long id) {
		this(name, id, new AtomicBoolean(true));
	}

	private SimpleContextualBatch(String name, long id, AtomicBoolean active) {
		super(name, status -> {
			active.set(false);
			LOGGER.log(System.Logger.Level.TRACE, "Closed batch {0}", id);
		});
		this.id = id;
		this.status = new Status() {
			@Override
			public boolean isActive() {
				return active.get();
			}

			@Override
			public boolean isDiscarding() {
				return false;
			}

			@Override
			public boolean isClosed() {
				return !active.get();
			}
		};
		LOGGER.log(System.Logger.Level.TRACE, "Created batch {0}", id);
	}

	@Override
	public Status getStatus() {
		return this.status;
	}

	@Override
	public ContextualBatch resume() {
		return this;
	}

	@Override
	public ContextualSuspendedBatch suspend() {
		return this;
	}

	@Override
	public void discard() {
		// Do nothing
	}

	@Override
	public int hashCode() {
		return Long.hashCode(this.id);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof SimpleContextualBatch batch)) return false;
		return this.id == batch.id;
	}

	@Override
	public String toString() {
		return String.valueOf(this.id);
	}
}
