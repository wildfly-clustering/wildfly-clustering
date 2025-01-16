/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.util.List;
import java.util.Set;

/**
 * Encapsulates the deployment container configuration for a test.
 * @author Paul Ferraro
 */
public interface DeploymentContainerConfiguration {

	DeploymentContainerRegistry getDeploymentContainerRegistry();

	/**
	 * Returns a list of deployment containers with the specified names.
	 * @param names a set of deployment container names
	 * @return a list of deployment containers with the specified names.
	 */
	default List<DeploymentContainer> getDeploymentContainers(Set<String> names) {
		return names.stream().map(this.getDeploymentContainerRegistry()::getContainer).sorted().toList();
	}

	/**
	 * Returns a list of all deployment containers.
	 * @return a list of all deployment containers.
	 */
	default List<DeploymentContainer> getDeploymentContainers() {
		return this.getDeploymentContainerRegistry().getContainers();
	}
}
