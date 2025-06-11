/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.batch;

import org.wildfly.clustering.function.Supplier;

/**
 * Instruments the batching of cache operations.
 * Write-only cache operations invoked while this batch is active (i.e. not suspended) will defer invocation until {@link Batch#close()}.
 * Calling {@link Batch#discard()} will cause {@link Batch#close()} to undo previous write operations and discard any accumulated write-only operations (similar to setRollbackOnly() semantics).
 * @author Paul Ferraro
 */
public interface Batch extends AutoCloseable {
	Batch CLOSED = new SimpleBatch(false);

	interface Factory extends Supplier<Batch> {
		Factory SIMPLE = new Factory() {
			@Override
			public Batch get() {
				return new SimpleBatch(true);
			}
		};
	}

	/**
	 * Suspends this batch.
	 * @return a suspended batch
	 */
	SuspendedBatch suspend();

	/**
	 * Suspends this batch until {@link BatchContext#close}.
	 * @return a suspended batch context
	 */
	default BatchContext<SuspendedBatch> suspendWithContext() {
		SuspendedBatch suspended = this.suspend();
		return new BatchContext<>() {
			@Override
			public SuspendedBatch get() {
				return suspended;
			}

			@Override
			public void close() {
				suspended.resume();
			}
		};
	}

	/**
	 * Discards this batch.  A discarded batch must still be closed.
	 */
	void discard();

	/**
	 * Indicates whether or not this batch is active.
	 * @return true, if this batch is active, false otherwise.
	 */
	boolean isActive();

	/**
	 * Indicates whether or not this batch will be discarded.
	 * @return true, if this batch was discarded, false otherwise.
	 */
	boolean isDiscarding();

	/**
	 * Indicates whether or not this batch was closed.
	 * @return true, if this batch was closed, false otherwise.
	 */
	boolean isClosed();

	@Override
	void close();
}
