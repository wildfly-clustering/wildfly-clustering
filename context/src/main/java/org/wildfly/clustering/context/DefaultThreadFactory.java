/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

import org.jboss.threads.JBossThreadFactory;

/**
 * Default {@link ThreadFactory} implementation that applies a specific context {@link ClassLoader}.
 * @author Paul Ferraro
 */
public class DefaultThreadFactory extends ContextualThreadFactory<ClassLoader> {

	public DefaultThreadFactory(Class<?> targetClass) {
		this(targetClass, () -> new ThreadGroup(targetClass.getSimpleName()));
	}

	public DefaultThreadFactory(Class<?> targetClass, Supplier<ThreadGroup> threadGroup) {
		this(new JBossThreadFactory(threadGroup.get(), Boolean.FALSE, null, "%G - %t", null, null), targetClass);
	}

	public DefaultThreadFactory(ThreadFactory factory) {
		this(factory, factory.getClass());
	}

	private DefaultThreadFactory(ThreadFactory factory, Class<?> targetClass) {
		super(factory, targetClass.getClassLoader(), ContextClassLoaderReference.INSTANCE);
	}
}
