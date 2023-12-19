/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.security.PrivilegedAction;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * @author Paul Ferraro
 */
class Reflect {

	static <T> void loadAll(Class<T> targetClass, Consumer<T> consumer) {
		java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Void run() {
				for (T provider : ServiceLoader.load(targetClass, targetClass.getClassLoader())) {
					consumer.accept(provider);
				}
				return null;
			}
		});
	}

	static <T> void loadSingle(Class<T> targetClass, ClassLoader loader, Consumer<T> consumer) {
		Optional<T> service = java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Optional<T> run() {
				return ServiceLoader.load(targetClass, loader).findFirst();
			}
		});
		service.ifPresent(consumer);
	}

	static ClassLoader setThreadContextClassLoader(ClassLoader loader) {
		Thread thread = Thread.currentThread();
		return java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				ClassLoader currentLoader = thread.getContextClassLoader();
				thread.setContextClassLoader(loader);
				return currentLoader;
			}
		});
	}
}
