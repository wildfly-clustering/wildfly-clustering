/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.util;

import java.time.Duration;
import java.util.List;

import org.jboss.logging.Logger;
import org.wildfly.common.function.ExceptionRunnable;
import org.wildfly.common.function.ExceptionSupplier;

/**
 * Defines a strategy for invoking a given action.
 * TODO Replace this with Failsafe
 * @author Paul Ferraro
 */
public interface Invoker {
	/**
	 * Invokes the specified action
	 * @param action an action to be invoked
	 * @return the result of the action
	 * @throws Exception if invocation fails
	 */
	<R, E extends Exception> R invoke(ExceptionSupplier<R, E> action) throws E;

	/**
	 * Invokes the specified action
	 * @param action an action to be invoked
	 * @throws Exception if invocation fails
	 */
	default <E extends Exception> void invoke(ExceptionRunnable<E> action) throws E {
		ExceptionSupplier<Void, E> adapter = new ExceptionSupplier<>() {
			@Override
			public Void get() throws E {
				action.run();
				return null;
			}
		};
		this.invoke(adapter);
	}

	/**
	 * Creates a direct invoker.
	 * @return a new invoker instance.
	 */
	static Invoker direct() {
		return new Invoker() {
			@Override
			public <R, E extends Exception> R invoke(ExceptionSupplier<R, E> action) throws E {
				return action.get();
			}

			@Override
			public <E extends Exception> void invoke(ExceptionRunnable<E> action) throws E {
				action.run();
			}
		};
	}

	/**
	 * Creates a retrying invoker, where retries are spaced using the specified backoff intervals.
	 * @return a new invoker instance.
	 */
	static Invoker retrying(List<Duration> intervals) {
		return new Invoker() {
			private final Logger logger = Logger.getLogger(Invoker.class);

			@Override
			public <R, E extends Exception> R invoke(ExceptionSupplier<R, E> task) throws E {
				int attempt = 0;
				for (Duration delay : intervals) {
					if (Thread.currentThread().isInterrupted()) break;
					try {
						return task.get();
					} catch (Exception e) {
						this.logger.debugf(e, "Attempt #%d failed", ++attempt);
					}
					if (delay.isZero() || delay.isNegative()) {
						Thread.yield();
					} else {
						try {
							Thread.sleep(delay.toMillis(), delay.getNano() % 1_000_000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				}
				return task.get();
			}
		};
	}
}
