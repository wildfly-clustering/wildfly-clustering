/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.util.function.Supplier;

/**
 * Thread-aware reference for a context {@link ClassLoader}.
 * @author Paul Ferraro
 */
public class ThreadContextClassLoaderReference extends ThreadContextReference<ClassLoader> {
	/** A context reference for the class loader of the current thread */
	public static final ContextReference<ClassLoader> CURRENT = new ThreadContextClassLoaderReference(Thread::currentThread);

	public ThreadContextClassLoaderReference(Supplier<Thread> reference) {
		super(reference, Thread::getContextClassLoader, Thread::setContextClassLoader);
	}
}
