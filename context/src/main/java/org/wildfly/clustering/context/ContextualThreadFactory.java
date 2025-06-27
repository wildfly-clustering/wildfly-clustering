/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ThreadFactory;

/**
 * {@link ThreadFactory} decorator that contextualizes its threads.
 * @param <C> the context type
 * @author Paul Ferraro
 */
public class ContextualThreadFactory<C> implements ThreadFactory {
	private final ThreadFactory factory;
	private final Contextualizer contextualizer;

	public ContextualThreadFactory(ThreadFactory factory, C targetContext, ContextReference<C> contextReference) {
		this(factory, Contextualizer.withContextProvider(contextReference.provide(targetContext)));
	}

	ContextualThreadFactory(ThreadFactory factory, Contextualizer contextualizer) {
		this.factory = factory;
		this.contextualizer = contextualizer;
	}

	@Override
	public Thread newThread(Runnable task) {
		@SuppressWarnings("removal")
		Thread thread = AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Thread run() {
				return ContextualThreadFactory.this.factory.newThread(ContextualThreadFactory.this.contextualizer.contextualize(task));
			}
		});
		return thread;
	}
}
