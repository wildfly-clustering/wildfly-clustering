/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A factory for creating thread context batches referencing simple batches.
 * @author Paul Ferraro
 */
public class SimpleContextualBatchFactory extends ThreadContextBatchFactory {

	public SimpleContextualBatchFactory(String name) {
		this(name, new AtomicLong(0L));
	}

	private SimpleContextualBatchFactory(String name, AtomicLong identifierFactory) {
		super(() -> new SimpleContextualBatch(name, identifierFactory.incrementAndGet()));
	}
}
