/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Methods requiring permission checking when a security manager is enabled.
 * @author Paul Ferraro
 */
class Privileged {

	static ClassLoader getClassLoader(Class<?> targetClass) {
		if (System.getSecurityManager() == null) {
			return targetClass.getClassLoader();
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return targetClass.getClassLoader();
			}
		});
	}
}
