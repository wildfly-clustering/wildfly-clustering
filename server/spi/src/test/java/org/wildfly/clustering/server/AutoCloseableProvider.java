/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Paul Ferraro
 */
public class AutoCloseableProvider implements AutoCloseable, Consumer<Runnable> {

	private final List<Runnable> tasks = new LinkedList<>();

	@Override
	public void accept(Runnable task) {
		this.tasks.add(0, task);
	}

	@Override
	public void close() {
		this.tasks.forEach(Runnable::run);
	}
}
