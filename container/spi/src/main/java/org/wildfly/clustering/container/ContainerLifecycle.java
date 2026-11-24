/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container;

import org.testcontainers.lifecycle.Startable;

/**
 * Encapsulates the lifecycle of a container.
 * @author Paul Ferraro
 */
public interface ContainerLifecycle extends Startable {
	/**
	 * An empty container lifecycle.
	 */
	ContainerLifecycle EMPTY = new ContainerLifecycle() {
		@Override
		public boolean isStarted() {
			return false;
		}

		@Override
		public void start() {
		}

		@Override
		public void stop() {
		}
	};

	/**
	 * Returns true if this container is started, false otherwise.
	 * @return true if this container is started, false otherwise.
	 */
	boolean isStarted();
}
