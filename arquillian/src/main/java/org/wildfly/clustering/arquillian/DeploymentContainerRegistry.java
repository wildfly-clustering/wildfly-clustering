/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.util.List;
import java.util.Set;

/**
 * The registry of deployment containers.
 * @author Paul Ferraro
 */
public interface DeploymentContainerRegistry {

	/**
	 * Returns the container with the specified name.
	 * @param name a container name
	 * @return the container with the specified name, or null, if no such container exists.
	 */
	DeploymentContainer getContainer(String name);

	/**
	 * Returns the collection of known deployment containers sorted by name.
	 * @return a collection of deployment containers.
	 */
	Set<String> getContainerNames();

	/**
	 * Returns a list of known deployment containers sorted by name.
	 * @return a list of deployment containers.
	 */
	default List<DeploymentContainer> getContainers() {
		return this.getContainers(this.getContainerNames());
	}

	/**
	 * Returns a list of the specified deployment containers sorted by name.
	 * @param names a set of deployment container names
	 * @return a list of deployment containers.
	 */
	List<DeploymentContainer> getContainers(Set<String> names);
}
