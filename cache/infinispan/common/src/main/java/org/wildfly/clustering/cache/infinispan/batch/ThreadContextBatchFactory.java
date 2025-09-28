/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;

/**
 * A factory that {@link ThreadContextBatch}.
 * @author Paul Ferraro
 */
public class ThreadContextBatchFactory implements Supplier<Batch> {
	private final Supplier<ContextualBatch> factory;

	/**
	 * Creates a batch factory whose batches are referenced via {@link ThreadLocal}.
	 * @param factory a batch factory
	 */
	public ThreadContextBatchFactory(Supplier<ContextualBatch> factory) {
		this.factory = factory;
	}

	@Override
	public Batch get() {
		ContextualBatch currentBatch = ThreadContextBatch.INSTANCE.get();
		// If a batch is already associated with this thread, extends its batch context, otherwise, associated a new batch with the current thread
		Supplier<ContextualBatch> factory = (currentBatch != null) ? currentBatch : this.factory;
		ThreadContextBatch.INSTANCE.accept(factory.get());
		return ThreadContextBatch.INSTANCE;
	}
}
