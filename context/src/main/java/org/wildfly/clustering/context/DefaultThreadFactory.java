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

	public DefaultThreadFactory(Class<?> targetClass, ClassLoader loader) {
		this(() -> new ThreadGroup(targetClass.getSimpleName()), loader);
	}

	public DefaultThreadFactory(Supplier<ThreadGroup> threadGroup, ClassLoader loader) {
		this(new JBossThreadFactory(threadGroup.get(), Boolean.FALSE, null, "%G - %t", null, null), loader);
	}

	public DefaultThreadFactory(ThreadFactory factory, ClassLoader loader) {
		super(factory, loader, ContextClassLoaderReference.INSTANCE);
	}
}
