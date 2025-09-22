/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.manager;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.service.Service;

/**
 * A manager of server-side state.
 * @param <I> the identifier type
 * @author Paul Ferraro
 */
public interface Manager<I> extends ManagerConfiguration<I>, Service {

	/**
	 * Returns a batcher for use by users of this manager.
	 * @return a batcher
	 */
	Supplier<Batch> getBatchFactory();
}
