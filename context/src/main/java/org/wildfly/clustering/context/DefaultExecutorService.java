/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

/**
 * {@link ExecutorService} that performs contextual execution of submitted tasks.
 * @author Paul Ferraro
 */
public class DefaultExecutorService extends ContextualExecutorService {

	/**
	 * Constructs a contextual executor service from the default contextualizer factory.
	 * @param factory a function creating an executor service for a given thread factory.
	 * @param loader the target class loader context
	 */
	@SuppressWarnings("removal")
	public DefaultExecutorService(Function<ThreadFactory, ExecutorService> factory, ClassLoader loader) {
		// Use thread group of current thread
		this(factory.apply(AccessController.doPrivileged(new PrivilegedAction<ThreadFactory>() {
			@Override
			public ThreadFactory run() {
				return new DefaultThreadFactory(Thread.currentThread().getThreadGroup(), DefaultExecutorService.class.getClassLoader());
			}
		})), loader);
	}

	/**
	 * Constructs a contextual executor service from the specified executor and class loader.
	 * @param executor the decorated executor service
	 * @param loader the target class loader context
	 */
	public DefaultExecutorService(ExecutorService executor, ClassLoader loader) {
		// Use thread group of current thread
		super(executor, DefaultContextualizerFactory.INSTANCE.createContextualizer(loader));
	}
}
