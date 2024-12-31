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

	@SuppressWarnings("removal")
	@Override
	public ClassLoader apply(Thread thread) {
		if (System.getSecurityManager() == null) {
			return thread.getContextClassLoader();
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return thread.getContextClassLoader();
			}
		});
	}

	@SuppressWarnings("removal")
	@Override
	public void accept(Thread thread, ClassLoader loader) {
		if (System.getSecurityManager() == null) {
			thread.setContextClassLoader(loader);
		} else {
			AccessController.doPrivileged(new PrivilegedAction<>() {
				@Override
				public Void run() {
					thread.setContextClassLoader(loader);
					return null;
				}
			});
		}
	}
}
