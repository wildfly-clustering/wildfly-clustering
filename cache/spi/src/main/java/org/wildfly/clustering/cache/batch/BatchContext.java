/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.batch;

import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Supplier;

/**
 * Encapsulates a batch context.
 * @author Paul Ferraro
 * @param <B> the type of this context
 */
public interface BatchContext<B> extends Supplier<B>, Context {
	/**
	 * Returns a batch context that performs the specified action on {@link Context#close()}.
	 * @param <B> the batch type
	 * @param batch the batch for this context
	 * @param closeAction the action to perform on close
	 * @return a new batch context for the specified batch.
	 */
	static <B> BatchContext<B> of(B batch, Consumer<B> closeAction) {
		return new BatchContext<>() {
			@Override
			public B get() {
				return batch;
			}

			@Override
			public void close() {
				closeAction.accept(batch);
			}
		};
	}
}
