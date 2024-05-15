/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.security.PrivilegedAction;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

/**
 * {@link ExecutorService} that performs contextual execution of submitted tasks.
 * @author Paul Ferraro
 */
public class DefaultExecutorService extends ContextualExecutorService {

	public static final PrivilegedAction<Void> shutdown(ExecutorService executor) {
		return new PrivilegedAction<>() {
			@Override
			public Void run() {
				executor.shutdown();
				return null;
			}
		};
	}

	public static final PrivilegedAction<List<Runnable>> shutdownNow(ExecutorService executor) {
		return new PrivilegedAction<>() {
			@Override
			public List<Runnable> run() {
				return executor.shutdownNow();
			}
		};
	}

	public DefaultExecutorService(Function<ThreadFactory, ExecutorService> factory, ClassLoader loader) {
		// Use thread group of current thread
		super(factory.apply(new DefaultThreadFactory(Thread.currentThread()::getThreadGroup, Reflect.getClassLoader(DefaultExecutorService.class))), DefaultContextualizerFactory.INSTANCE.createContextualizer(loader));
	}
}
