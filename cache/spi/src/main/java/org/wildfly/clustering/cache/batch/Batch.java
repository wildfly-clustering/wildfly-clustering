/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.batch;

import org.wildfly.clustering.context.Context;

/**
 * Instruments the batching of cache operations.
 * Write-only cache operations invoked while this batch is active (i.e. not suspended) will defer invocation until {@link Batch#close()}.
 * Calling {@link Batch#discard()} will cause {@link Batch#close()} to undo previous write operations and discard any accumulated write-only operations (similar to setRollbackOnly() semantics).
 * @author Paul Ferraro
 */
public interface Batch extends AutoCloseable {

	interface Status {
		/**
		 * Indicates whether or not this batch is active.
		 * @return true, if this batch is active, false otherwise.
		 */
		boolean isActive();

		/**
		 * Indicates whether or not this batch will be discarded.
		 * @return true, if this batch will be discarded, false otherwise.
		 */
		boolean isDiscarding();

		/**
		 * Indicates whether or not this batch was closed.
		 * @return true, if this batch was closed, false otherwise.
		 */
		boolean isClosed();
	}

	Status getStatus();

	/**
	 * Suspends this batch.
	 * @return a suspended batch
	 */
	SuspendedBatch suspend();

	/**
	 * Suspends this batch until {@link Context#close}.
	 * @return a suspended batch context
	 */
	default Context<SuspendedBatch> suspendWithContext() {
		SuspendedBatch suspended = this.suspend();
		return Context.of(suspended, SuspendedBatch::resume);
	}

	/**
	 * Discards this batch.  A discarded batch must still be closed.
	 */
	void discard();

	@Override
	void close();
}
