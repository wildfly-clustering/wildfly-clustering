/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import org.wildfly.clustering.cache.batch.SimpleBatchFactory;
import org.wildfly.clustering.function.Supplier;

/**
 * A factory that creates simple nestable batches.
 * @author Paul Ferraro
 */
public class SimpleExtendableBatchFactory extends SimpleBatchFactory<ParentBatch> {
	public static final Supplier<ParentBatch> INSTANCE = new SimpleExtendableBatchFactory();

	private SimpleExtendableBatchFactory() {
		super(SimpleExtendableBatch::new);
	}
}
