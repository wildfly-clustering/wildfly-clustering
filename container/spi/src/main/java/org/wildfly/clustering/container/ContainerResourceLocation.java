/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container;

/**
 * Encapsulates the location of a container resource.
 * @author Paul Ferraro
 * @param <T> the resource locator type
 */
public interface ContainerResourceLocation<T> {
	/**
	 * Returns the locator of a container resource for use within the container network.
	 * @return the locator of a container resource for use within the container network.
	 */
	T getLocator();

	/**
	 * Returns the locator of a container resource for use outside the container network.
	 * @return the locator of a container resource for use outside the container network.
	 */
	T getExternalLocator();
}
