/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;

/**
 * An OCI container lifecycle.
 * @author Paul Ferraro
 */
public interface LifecycleFactory {
	/** A default lifecycle factory */
	LifecycleFactory DEFAULT = new LifecycleFactory() {
		@Override
		public Lifecycle createContainerLifecycle(Container<?> container) {
			return new Lifecycle() {
				@Override
				public boolean isStarted() {
					return container.getState() == Container.State.STARTED;
				}

				@Override
				public void start() {
					try {
						container.start();
					} catch (LifecycleException e) {
						throw new IllegalStateException(e);
					}
				}

				@Override
				public void stop() {
					try {
						container.stop();
					} catch (LifecycleException e) {
						throw new IllegalStateException(e);
					}
				}
			};
		}
	};

	/**
	 * Creates the lifecycle for the specified container.
	 * @param container an OCI container
	 * @return the lifecycle for the specified container.
	 */
	Lifecycle createContainerLifecycle(Container<?> container);
}
