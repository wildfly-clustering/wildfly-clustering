/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.SuspendedBatch;
import org.wildfly.clustering.context.ContextReference;

/**
 * A batch referenced via {@link ThreadLocal}.
 * @author Paul Ferraro
 */
enum ThreadContextBatch implements Batch, ContextReference<Batch> {
	INSTANCE;

	private static final ThreadLocal<Batch> THREAD_CONTEXT = new ThreadLocal<>();

	@Override
	public void accept(Batch batch) {
		if (batch != null) {
			THREAD_CONTEXT.set(batch);
		} else {
			THREAD_CONTEXT.remove();
		}
	}

	@Override
	public Batch get() {
		return THREAD_CONTEXT.get();
	}

	@Override
	public SuspendedBatch suspend() {
		Batch batch = this.get();
		SuspendedBatch suspended = (batch != null) ? batch.suspend() : null;
		if (batch != null) {
			this.accept(null);
		}
		return new SuspendedBatch() {
			@Override
			public Batch resume() {
				Batch current = ThreadContextBatch.this.get();
				if (current != null) {
					// As with TransactionManager.resume(...), it is illegal to resume a batch if the current thread is already associated with a batch
					throw new IllegalStateException(current.toString());
				}
				Batch resumed = (suspended != null) ? suspended.resume() : null;
				ThreadContextBatch.this.accept(resumed);
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
	public boolean isActive() {
		Batch batch = this.get();
		return (batch != null) ? batch.isActive() : false;
	}

	@Override
	public boolean isDiscarding() {
		Batch batch = this.get();
		return (batch != null) ? batch.isDiscarding() : false;
	}

	@Override
	public boolean isClosed() {
		Batch batch = this.get();
		return (batch != null) ? batch.isClosed() : true;
	}

	@Override
	public void close() {
		Batch batch = this.get();
		if (batch != null) {
			batch.close();
			if (batch.isClosed()) {
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
