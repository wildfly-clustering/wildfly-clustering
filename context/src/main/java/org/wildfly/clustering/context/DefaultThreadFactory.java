/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

/**
 * Default {@link ThreadFactory} implementation that applies a specific context {@link ClassLoader}.
 * @author Paul Ferraro
 */
public class DefaultThreadFactory extends ContextualThreadFactory<ClassLoader> {

	public DefaultThreadFactory(Class<?> targetClass) {
		this(targetClass, () -> new ThreadGroup(targetClass.getSimpleName()));
	}

	public DefaultThreadFactory(Class<?> targetClass, Supplier<ThreadGroup> threadGroup) {
		this(Reflect.createThreadFactory(threadGroup), targetClass);
	}

	public DefaultThreadFactory(ThreadFactory factory) {
		this(factory, factory.getClass());
	}

	private DefaultThreadFactory(ThreadFactory factory, Class<?> targetClass) {
		super(factory, Reflect.getClassLoader(targetClass), ContextClassLoaderReference.INSTANCE);
	}
}
