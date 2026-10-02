/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container.arquillian;

import org.wildfly.clustering.arquillian.Lifecycle;
import org.wildfly.clustering.container.ContainerLifecycle;

/**
 * Lifecycle facade for an OCI container.
 * @author Paul Ferraro
 */
class RemoteContainerLifecycle implements Lifecycle {
	private final ContainerLifecycle container;

	RemoteContainerLifecycle(ContainerLifecycle container) {
		this.container = container;
	}

	@Override
	public void start() {
		this.container.start();
	}

	@Override
	public void stop() {
		this.container.stop();
	}

	@Override
	public boolean isStarted() {
		return this.container.isStarted();
	}

	@Override
	public void close() {
		this.container.close();
	}
}
