/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.context;

import java.util.function.Supplier;

import org.wildfly.clustering.function.Runnable;

/**
 * Encapsulates some context that is applicable until {@link #close()}.
 * @author Paul Ferraro
 * @param <T> the context value type
 */
public interface Context<T> extends Supplier<T>, AutoCloseable {
	/** An empty context */
	Context<?> EMPTY = of(null, Runnable.empty());

	@Override
	void close();

	/**
	 * Returns an empty context.
	 * @param <T> the context value type
	 * @return an empty context.
	 */
	@SuppressWarnings("unchecked")
	static <T> Context<T> empty() {
		return (Context<T>) EMPTY;
	}

	/**
	 * Returns a context that provides the specified value and invokes the specified task on close.
	 * @param value the context value
	 * @param closeTask the action to perform on {@link #close()}.
	 * @param <T> the context value type
	 * @return a context that provides the specified value and invokes the specified task on close.
	 */
	static <T> Context<T> of(T value, java.lang.Runnable closeTask) {
		return new Context<>() {
			@Override
			public T get() {
				return value;
			}

			@Override
			public void close() {
				closeTask.run();
			}
		};
	}
}
