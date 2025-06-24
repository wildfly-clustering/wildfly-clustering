/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.cache.batch.BatchContextualizerFactory;
import org.wildfly.clustering.context.Contextualizer;
import org.wildfly.clustering.context.ContextualizerFactory;

/**
 * A contextualizer for a batch.
 * @author Paul Ferraro
 */
@MetaInfServices({ ContextualizerFactory.class, BatchContextualizerFactory.class })
public class TransactionBatchContextualizerFactory implements BatchContextualizerFactory {

	@Override
	public Contextualizer createContextualizer(ClassLoader loader) {
		return Contextualizer.withContextProvider(ThreadContextBatch.INSTANCE::suspendWithContext);
	}
}
