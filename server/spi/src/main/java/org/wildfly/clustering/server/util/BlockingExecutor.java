/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

/**
 * Allows safe invocation of tasks that require resources not otherwise available after {@link #close()} to block a service from stopping.
 * @author Paul Ferraro
 */
public interface BlockingExecutor extends Executor, AutoCloseable {

	/**
	 * Executes the specified task, but only if the service was not already closed.
	 * If service is already closed, the task is not run.
	 * If executed, the specified task must return a non-null value, to be distinguishable from a non-execution.
	 * @param executeTask a task to execute
	 * @return an optional value that is present only if the specified task was run.
	 */
	<R> Optional<R> execute(Supplier<R> executeTask);

	/**
	 * Executes the specified task, but only if the service was not already closed.
	 * If service is already closed, the task is not run.
	 * If executed, the specified task must return a non-null value, to be distinguishable from a non-execution.
	 * @param executeTask a task to execute
	 * @throws Exception if the specified task fails to execute
	 * @return an optional value that is present only if the specified task was run.
	 */
	<R> Optional<R> execute(Callable<R> executeTask) throws Exception;

	@Override
	void close();

	/**
	 * Creates new blocking executor that runs the specified task upon {@link #close()}.
	 * The specified task will only execute once, upon the first {@link #close()} invocation.
	 * @param closeTask a task to run when this executor is closed.
	 * @return a new blocking executor
	 */
	static BlockingExecutor newInstance(Runnable closeTask) {
		return new BlockingExecutor() {
			private final StampedLock lock = new StampedLock();
			private final AtomicBoolean closed = new AtomicBoolean(false);

			@Override
			public void execute(Runnable executeTask) {
				long stamp = this.lock.tryReadLock();
				if (stamp != 0L) {
					try {
						executeTask.run();
					} finally {
						this.lock.unlock(stamp);
					}
				}
			}

			@Override
			public <R> Optional<R> execute(Supplier<R> executeTask) {
				long stamp = this.lock.tryReadLock();
				if (stamp != 0L) {
					try {
						return Optional.of(executeTask.get());
					} finally {
						this.lock.unlock(stamp);
					}
				}
				return Optional.empty();
			}

			@Override
			public <R> Optional<R> execute(Callable<R> executeTask) throws Exception {
				long stamp = this.lock.tryReadLock();
				if (stamp != 0L) {
					try {
						return Optional.of(executeTask.call());
					} finally {
						this.lock.unlock(stamp);
					}
				}
				return Optional.empty();
			}

			@Override
			public void close() {
				// Allow only one thread to close
				if (this.closed.compareAndSet(false, true)) {
					// Closing is final - we don't need the stamp
					this.lock.writeLock();
					closeTask.run();
				}
			}
		};
	}
}
