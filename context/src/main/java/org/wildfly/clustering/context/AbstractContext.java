/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.context;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * An abstract context that can accumulate actions to run on close.
 * @author Paul Ferraro
 * @param <T> the context value type
 */
public abstract class AbstractContext<T> implements Context<T>, Consumer<Runnable> {

	private final Deque<Runnable> tasks = new LinkedList<>();

	/**
	 * Constructs a new context.
	 */
	protected AbstractContext() {
		// Do nothing
	}

	@Override
	public void accept(Runnable task) {
		this.tasks.add(task);
	}

	@Override
	public void close() {
		org.wildfly.clustering.function.Runnable.runAll(this.tasks::descendingIterator).run();
	}
}
