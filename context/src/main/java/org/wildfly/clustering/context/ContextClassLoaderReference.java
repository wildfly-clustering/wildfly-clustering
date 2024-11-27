/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Thread-aware reference for a context {@link ClassLoader}.
 * @author Paul Ferraro
 */
public enum ContextClassLoaderReference implements ThreadContextReference<ClassLoader> {
	INSTANCE;

	@Override
	public ClassLoader apply(Thread thread) {
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return thread.getContextClassLoader();
			}
		});
	}

	@Override
	public void accept(Thread thread, ClassLoader loader) {
		AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Void run() {
				thread.setContextClassLoader(loader);
				return null;
			}
		});
	}
}
