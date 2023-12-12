/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.manager;

import java.util.function.Supplier;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.Batcher;

/**
 * Configuration of a state manager.
 * @author Paul Ferraro
 */
public interface ManagerConfiguration<I, B extends Batch> {

	/**
	 * Returns a factory for creating identifiers for use by this manager.
	 * @return an identifier factory
	 */
	Supplier<I> getIdentifierFactory();

	/**
	 * Returns a batcher for use by users of this manager.
	 * @return a batcher
	 */
	Batcher<B> getBatcher();
}
