/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import java.util.concurrent.atomic.AtomicInteger;

import org.wildfly.clustering.cache.batch.SimpleBatch;

/**
 * A simple extendable batch with no suspend/resume behavior.
 * @author Paul Ferraro
 */
public class SimpleExtendableBatch extends SimpleBatch implements ParentBatch, SuspendedParentBatch {
	private final AtomicInteger count = new AtomicInteger(0);
	private final long id;

	SimpleExtendableBatch(long id) {
		this(id, true);
	}

	SimpleExtendableBatch(long id, boolean active) {
		super(id, active);
		this.id = id;
	}

	@Override
	public ParentBatch resume() {
		return this;
	}

	@Override
	public SuspendedParentBatch suspend() {
		return this;
	}

	@Override
	public ParentBatch get() {
		long count = this.count.incrementAndGet();
		LOGGER.log(System.Logger.Level.DEBUG, "Created extended batch {0}[{1}]", this.id, count);
		return this;
	}

	@Override
	public void close() {
		int count = this.count.getAndDecrement();
		if (count == 0) {
			super.close();
		} else {
			LOGGER.log(System.Logger.Level.DEBUG, "Closed extended batch {0}[{1}]", this.id, count);
		}
	}
}
