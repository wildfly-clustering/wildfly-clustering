/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

/**
 * {@link ExecutorService} that performs contextual execution of submitted tasks.
 * @author Paul Ferraro
 */
public class DefaultExecutorService extends ContextualExecutorService {

	public DefaultExecutorService(Function<ThreadFactory, ExecutorService> factory, ClassLoader loader) {
		// Use thread group of current thread
		super(factory.apply(new DefaultThreadFactory(Thread.currentThread()::getThreadGroup, DefaultExecutorService.class.getClassLoader())), DefaultContextualizerFactory.INSTANCE.createContextualizer(loader));
	}
}
