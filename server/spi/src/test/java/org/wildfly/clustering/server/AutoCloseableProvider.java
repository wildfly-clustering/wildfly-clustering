/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * @author Paul Ferraro
 */
public class AutoCloseableProvider implements AutoCloseable, Consumer<Runnable> {

	private final Deque<Runnable> tasks = new LinkedList<>();

	@Override
	public void accept(Runnable task) {
		this.tasks.add(task);
	}

	@Override
	public void close() {
		this.tasks.descendingIterator().forEachRemaining(Runnable::run);
	}
}
