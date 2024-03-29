/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

/**
 * Thread-aware reference for a context {@link ClassLoader}.
 * @author Paul Ferraro
 */
public enum ContextClassLoaderReference implements ThreadContextReference<ClassLoader> {
	INSTANCE;

	@Override
	public ClassLoader apply(Thread thread) {
		return thread.getContextClassLoader();
	}

	@Override
	public void accept(Thread thread, ClassLoader loader) {
		thread.setContextClassLoader(loader);
	}
}
