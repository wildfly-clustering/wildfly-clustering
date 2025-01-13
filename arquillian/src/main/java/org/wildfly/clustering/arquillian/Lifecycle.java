/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.util.Collection;

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
			this.stop();
		}
	}

	/**
	 * Returns a composite lifecycle object, useful for performing bulk lifecycle operations.
	 * @param lifecycles a collection of lifecycle objects
	 * @return a composite lifecycle
	 */
	static Lifecycle composite(Collection<? extends Lifecycle> lifecycles) {
		return new Lifecycle() {
			@Override
			public boolean isStarted() {
				return lifecycles.stream().anyMatch(Lifecycle::isStarted);
			}

			@Override
			public void start() {
				lifecycles.forEach(Lifecycle::start);
			}

			@Override
			public void stop() {
				lifecycles.forEach(Lifecycle::stop);
			}

			@Override
			public void close() {
				lifecycles.stream().forEach(Lifecycle::close);
			}
		};
	}
}
