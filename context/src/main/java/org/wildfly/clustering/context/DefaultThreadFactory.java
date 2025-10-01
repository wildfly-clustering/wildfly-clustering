/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default {@link ThreadFactory} implementation that applies a specific context {@link ClassLoader}.
 * @author Paul Ferraro
 */
public class DefaultThreadFactory extends ContextualThreadFactory<ClassLoader> {

	/**
	 * Creates a new thread factory for the specified class using the specified context class loader.
	 * @param targetClass the class from which a thread group will be created.
	 * @param loader the class loader context
	 */
	@SuppressWarnings("removal")
	public DefaultThreadFactory(Class<?> targetClass, ClassLoader loader) {
		this(AccessController.doPrivileged(new PrivilegedAction<ThreadGroup>() {
			@Override
			public ThreadGroup run() {
				return new ThreadGroup(targetClass.getSimpleName());
			}
		}), loader);
	}

	/**
	 * Creates a new thread factory using the specified thread group and context class loader.
	 * @param group the thread group for threads created by this factory
	 * @param loader the class loader context
	 */
	public DefaultThreadFactory(ThreadGroup group, ClassLoader loader) {
		this(new ThreadGroupThreadFactory(group), loader);
	}

	/**
	 * Creates a new thread factory using the specified context class loader.
	 * @param factory the decorated thread factory
	 * @param loader the class loader context
	 */
	public DefaultThreadFactory(ThreadFactory factory, ClassLoader loader) {
		super(factory, loader, ThreadContextClassLoaderReference.CURRENT);
	}

	private static class ThreadGroupThreadFactory implements ThreadFactory {
		private final AtomicLong index = new AtomicLong();
		private final ThreadGroup group;

		ThreadGroupThreadFactory(ThreadGroup group) {
			this.group = group;
		}

		@SuppressWarnings("removal")
		@Override
		public Thread newThread(Runnable task) {
			ThreadGroup group = this.group;
			String name = String.format("%s - %d", this.group.getName(), this.index.incrementAndGet());
			return AccessController.doPrivileged(new PrivilegedAction<>() {
				@Override
				public Thread run() {
					return new Thread(group, task, name);
				}
			});
		}
	}
}
