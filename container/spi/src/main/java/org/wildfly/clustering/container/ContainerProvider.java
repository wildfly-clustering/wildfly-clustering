/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container;

import org.testcontainers.lifecycle.Startable;

/**
 * Provides an OCI container.
 * @author Paul Ferraro
 * @param <C> the OCI container type
 */
public interface ContainerProvider<C extends Startable> {
	/**
	 * Returns the provided container.
	 * @return the provided container.
	 */
	C getContainer();
}
