/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.transaction;

import java.util.Deque;
import java.util.Iterator;

import jakarta.transaction.Synchronization;

/**
 * A composite synchronization that invokes {@link #beforeCompletion()} in ascending order, and {@link #afterCompletion(int)} in descending order.
 * @author Paul Ferraro
 */
public class CompositeSynchronization implements Synchronization {
	private final Deque<Synchronization> synchronizations;

	/**
	 * Constructs a composite synchronization
	 * @param synchronizations a double-ended queue of synchronizations.
	 */
	public CompositeSynchronization(Deque<Synchronization> synchronizations) {
		this.synchronizations = synchronizations;
	}

	@Override
	public void beforeCompletion() {
		Iterator<Synchronization> synchronizations = this.synchronizations.iterator();
		while (synchronizations.hasNext()) {
			synchronizations.next().beforeCompletion();
		}
	}

	@Override
	public void afterCompletion(int status) {
		Iterator<Synchronization> synchronizations = this.synchronizations.descendingIterator();
		while (synchronizations.hasNext()) {
			synchronizations.next().afterCompletion(status);
		}
	}
}
