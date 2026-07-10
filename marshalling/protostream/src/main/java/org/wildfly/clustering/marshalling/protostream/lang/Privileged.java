/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.lang;

import java.security.PrivilegedAction;
import java.util.function.Supplier;

/**
 * Methods requiring permission checking when a security manager is enabled.
 * @author Paul Ferraro
 */
class Privileged {
	private Privileged() {
		// Hide
	}

	static ClassLoader getClassLoader(Class<?> targetClass) {
		return getClassLoader(targetClass::getClassLoader);
	}

	static ClassLoader getClassLoader(Module module) {
		return getClassLoader(module::getClassLoader);
	}

	@SuppressWarnings("removal")
	private static ClassLoader getClassLoader(Supplier<ClassLoader> provider) {
		if (System.getSecurityManager() == null) {
			return provider.get();
		}
		return java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return provider.get();
			}
		});
	}
}
