/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

/**
 * @author Paul Ferraro
 */
public interface DeploymentContainerRegistry {

	DeploymentContainer getContainer(String name);

	Iterable<DeploymentContainer> getContainers();
}
