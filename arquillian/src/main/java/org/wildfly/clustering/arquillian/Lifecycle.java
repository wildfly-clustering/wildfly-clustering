/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;

/**
 * Implemented by objects with an unmanaged lifecycle.
 * @author Paul Ferraro
 */
public interface Lifecycle extends AutoCloseable {

	/**
	 * Starts this object.
	 */
	void start();

	/**
	 * Stops this object.
	 */
	void stop();

	/**
	 * Indicates whether this object is started.
	 * @return true, if this object is started, false otherwise.
	 */
	boolean isStarted();

	/**
	 * Stops this object, if started.
	 */
	@Override
	default void close() {
		if (this.isStarted()) {
			try {
				this.stop();
			} catch (RuntimeException | Error e) {
				System.getLogger(this.getClass().getName()).log(System.Logger.Level.WARNING, e);
			}
		}
	}

	/**
	 * Returns a composite lifecycle object, useful for performing bulk lifecycle operations.
	 * @param lifecycles a collection of lifecycle objects
	 * @return a composite lifecycle
	 */
	static Lifecycle composite(List<? extends Lifecycle> lifecycles) {
		return new Lifecycle() {
			@Override
			public boolean isStarted() {
				return lifecycles.stream().anyMatch(Lifecycle::isStarted);
			}

			@Override
			public void start() {
				Deque<Lifecycle> started = new ArrayDeque<>(lifecycles.size());
				try {
					for (Lifecycle lifecycle : lifecycles) {
						if (!lifecycle.isStarted()) {
							lifecycle.start();
							started.add(lifecycle);
						}
					}
				} catch (RuntimeException | Error e) {
					started.descendingIterator().forEachRemaining(Lifecycle::close);
					throw e;
				}
			}

			@Override
			public void stop() {
				ListIterator<? extends Lifecycle> iterator = lifecycles.listIterator(lifecycles.size());
				while (iterator.hasPrevious()) {
					Lifecycle lifecycle = iterator.previous();
					if (lifecycle.isStarted()) {
						lifecycle.stop();
					}
				}
			}

			@Override
			public void close() {
				ListIterator<? extends Lifecycle> iterator = lifecycles.listIterator(lifecycles.size());
				while (iterator.hasPrevious()) {
					iterator.previous().close();
				}
			}
		};
	}
}
