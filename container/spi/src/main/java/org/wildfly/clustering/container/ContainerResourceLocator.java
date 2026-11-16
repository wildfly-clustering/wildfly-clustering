/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container;

/**
 * A locator of a container resource.
 * @author Paul Ferraro
 * @param <T> the resource location type
 */
public interface ContainerResourceLocator<T> {
	/**
	 * Returns the location of a container resource.
	 * @return the location of a container resource.
	 */
	T getLocation();
}
