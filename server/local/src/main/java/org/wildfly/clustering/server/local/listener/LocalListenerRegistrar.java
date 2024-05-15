/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.listener;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jboss.logging.Logger;
import org.wildfly.clustering.context.DefaultExecutorService;
import org.wildfly.clustering.context.ExecutorServiceFactory;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.listener.ListenerRegistrar;

/**
 * Local {@link ListenerRegistrar}.
 * @param <T> the listener type
 * @author Paul Ferraro
 */
public class LocalListenerRegistrar<T> implements ListenerRegistrar<T> {
	private static final Logger LOGGER = Logger.getLogger(LocalListenerRegistrar.class);

	private final Map<T, ExecutorService> listeners = new ConcurrentHashMap<>();
	private final Duration shutdownTimeout;
	private final Function<T, ExecutorService> executorFactory = new Function<>() {
		@Override
		public ExecutorService apply(T listener) {
			java.security.PrivilegedAction<ClassLoader> action = Thread.currentThread()::getContextClassLoader;
			ClassLoader loader = java.security.AccessController.doPrivileged(action);
			return new DefaultExecutorService(ExecutorServiceFactory.SINGLE_THREAD, loader);
		}
	};

	public LocalListenerRegistrar(Duration shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
	}

	@Override
	public Registration register(T listener) {
		this.listeners.computeIfAbsent(listener, this.executorFactory);
		return () -> this.unregister(listener);
	}

	@Override
	public void accept(Consumer<T> event) {
		try {
			for (Map.Entry<T, ExecutorService> entry : this.listeners.entrySet()) {
				T listener = entry.getKey();
				Executor executor = entry.getValue();
				try {
					executor.execute(() -> event.accept(listener));
				} catch (RejectedExecutionException e) {
					// Listener was unregistered
				}
			}
		} catch (Throwable e) {
			LOGGER.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void close() {
		// Cleanup any stray listeners
		for (ExecutorService executor : this.listeners.values()) {
			this.shutdown(executor);
		}
		this.listeners.clear();
	}

	private void unregister(T listener) {
		ExecutorService executor = this.listeners.remove(listener);
		if (executor != null) {
			this.shutdown(executor);
		}
	}

	private void shutdown(ExecutorService executor) {
		executor.shutdown();
		try {
			executor.awaitTermination(this.shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
