/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import java.util.List;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.SuspendedBatch;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.context.ContextReference;
import org.wildfly.clustering.function.Runner;

/**
 * A batch referenced via {@link ThreadLocal}.
 * @author Paul Ferraro
 */
enum ThreadContextBatch implements Batch, ContextReference<ContextualBatch> {
	INSTANCE;

	private static final ThreadLocal<ContextualBatch> THREAD_CONTEXT = new ThreadLocal<>();
	private static final Status CLOSED_STATUS = new Status() {
		@Override
		public boolean isActive() {
			return false;
		}

		@Override
		public boolean isDiscarding() {
			return false;
		}

		@Override
		public boolean isClosed() {
			return true;
		}
	};

	@Override
	public void accept(ContextualBatch batch) {
		if (batch != null) {
			THREAD_CONTEXT.set(batch);
		} else {
			THREAD_CONTEXT.remove();
		}
	}

	@Override
	public ContextualBatch get() {
		return THREAD_CONTEXT.get();
	}

	@Override
	public Status getStatus() {
		Batch batch = this.get();
		return (batch != null) ? batch.getStatus() : CLOSED_STATUS;
	}

	@Override
	public SuspendedBatch suspend() {
		ContextualBatch batch = this.get();
		ContextualSuspendedBatch suspended = (batch != null) ? batch.suspend() : null;
		if (batch != null) {
			this.accept(null);
		}
		return new SuspendedBatch() {
			@Override
			public Context<Batch> resumeWithContext() {
				// Auto-suspend any active tx, and auto-resume on context close
				SuspendedBatch suspended = ThreadContextBatch.this.suspend();
				Batch resumed = this.resume();
				return Context.of(resumed, Runner.runAll(List.of(resumed::suspend, suspended::resume)));
			}

			@Override
			public Batch resume() {
				ContextualBatch current = ThreadContextBatch.this.get();
				if (current != null) {
					// As with TransactionManager.resume(...), it is illegal to resume a batch if the current thread is already associated with a batch
					throw new IllegalStateException(this.toString(), new ContextualException(current));
				}
				ThreadContextBatch.this.accept((suspended != null) && !batch.getStatus().isClosed() ? suspended.resume() : null);
				return INSTANCE;
			}
		};
	}

	@Override
	public void discard() {
		Batch batch = this.get();
		if (batch != null) {
			batch.discard();
		}
	}

	@Override
	public void close() {
		Batch batch = this.get();
		if (batch != null) {
			batch.close();
			if (batch.getStatus().isClosed()) {
				// Disassociate from thread if actually closed
				this.accept(null);
			}
		}
	}

	@Override
	public String toString() {
		Batch batch = this.get();
		return (batch != null) ? batch.toString() : "none";
	}
}
