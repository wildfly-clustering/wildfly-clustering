/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.cache.batch.BatchContextualizerFactory;
import org.wildfly.clustering.context.ContextReference;
import org.wildfly.clustering.context.Contextualizer;
import org.wildfly.clustering.context.ContextualizerFactory;

/**
 * A contextualizer for a batch.
 * @author Paul Ferraro
 */
@MetaInfServices({ ContextualizerFactory.class, BatchContextualizerFactory.class })
public class TransactionBatchContextualizerFactory implements BatchContextualizerFactory, ContextReference<TransactionBatch> {

	@Override
	public void accept(TransactionBatch batch) {
		TransactionBatch existing = ThreadLocalTransactionBatch.getCurrentBatch();
		if (existing != null) {
			existing.suspend();
		}
		if (batch != null) {
			batch.resume();
		}
	}

	@Override
	public TransactionBatch get() {
		TransactionBatch batch = ThreadLocalTransactionBatch.getCurrentBatch();
		if (batch != null) {
			batch.suspend();
		}
		return batch;
	}

	@Override
	public Contextualizer createContextualizer(ClassLoader loader) {
		return Contextualizer.withContext(ThreadLocalTransactionBatch.getCurrentBatch(), this);
	}
}
