/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import java.util.concurrent.atomic.AtomicLong;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;

/**
 * A factory that creates simple nestable batches.
 * @author Paul Ferraro
 */
public class SimpleContextualBatchFactory implements Supplier<Batch> {
	private final AtomicLong idFactory = new AtomicLong(0L);
	private final String name;

	public SimpleContextualBatchFactory(String name) {
		this.name = name;
	}

	@Override
	public Batch get() {
		TransactionalBatch batch = ThreadContextBatch.INSTANCE.get(TransactionalBatch.class);
		// If there is already an active batch associated with this thread, create a nested batch, otherwise, create a new transactional batch
		ThreadContextBatch.INSTANCE.accept((batch != null) ? batch.get() : new SimpleContextualBatch(this.name, this.idFactory.incrementAndGet()));
		return ThreadContextBatch.INSTANCE;
	}
}
