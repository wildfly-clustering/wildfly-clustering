/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.listener;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

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
	private static final System.Logger LOGGER = System.getLogger(LocalListenerRegistrar.class.getName());

	private final Map<T, ExecutorService> listeners = new ConcurrentHashMap<>();
	private final Duration shutdownTimeout;
	private final Function<T, ExecutorService> executorFactory = new Function<>() {
		@Override
		public ExecutorService apply(T listener) {
			return new DefaultExecutorService(ExecutorServiceFactory.SINGLE_THREAD, Thread.currentThread().getContextClassLoader());
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
			LOGGER.log(System.Logger.Level.ERROR, e.getLocalizedMessage(), e);
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

	@SuppressWarnings({ "removal" })
	private void shutdown(ExecutorService executor) {
		PrivilegedAction<Void> action = new PrivilegedAction<>() {
			@Override
			public Void run() {
				executor.shutdown();
				return null;
			}
		};
		AccessController.doPrivileged(action);
		try {
			executor.awaitTermination(this.shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
