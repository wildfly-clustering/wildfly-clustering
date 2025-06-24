/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.batch;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongFunction;

import org.wildfly.clustering.function.Supplier;

/**
 * Factory for creating simple batches.
 * @author Paul Ferraro
 * @param <B> the batch type
 */
public class SimpleBatchFactory<B extends Batch> implements Supplier<B> {
	public static final Supplier<Batch> INSTANCE = new SimpleBatchFactory<>(SimpleBatch::new);

	private final LongFunction<B> factory;
	private final AtomicLong identifierFactory = new AtomicLong(0);

	protected SimpleBatchFactory(LongFunction<B> factory) {
		this.factory = factory;
	}

	@Override
	public B get() {
		return this.factory.apply(this.identifierFactory.incrementAndGet());
	}
}
