/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container;

/**
 * Exposes the location of a container resource for use by a client.
 * @author Paul Ferraro
 * @param <I> the locator identifier type
 */
public interface ContainerResourceLocator<I> {
	/**
	 * Returns an identifier of a container resource.
	 * @return an identifier of a container resource.
	 */
	I getURI();
}
