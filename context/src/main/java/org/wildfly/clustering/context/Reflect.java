/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.context;

import java.security.PrivilegedAction;
import java.util.ServiceLoader;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jboss.threads.JBossThreadFactory;

/**
 * @author Paul Ferraro
 */
class Reflect {

	static ClassLoader getClassLoader(Class<?> targetClass) {
		return java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return targetClass.getClassLoader();
			}
		});
	}

	static ThreadFactory createThreadFactory(Supplier<ThreadGroup> group) {
		return java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ThreadFactory run() {
				return new JBossThreadFactory(group.get(), Boolean.FALSE, null, "%G - %t", null, null);
			}
		});
	}

	static ClassLoader getContextClassLoader(Thread thread) {
		return java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return thread.getContextClassLoader();
			}
		});
	}

	static ClassLoader setContextClassLoader(Thread thread, ClassLoader loader) {
		return java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				ClassLoader current = thread.getContextClassLoader();
				thread.setContextClassLoader(loader);
				return current;
			}
		});
	}

	static <T> void load(Class<T> targetClass, Consumer<T> consumer) {
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
}
