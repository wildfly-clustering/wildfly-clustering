/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.util.Collection;

/**
 * @author Paul Ferraro
 */
public interface DeploymentContainerRegistry {

	DeploymentContainer getContainer(String name);

	Collection<DeploymentContainer> getContainers();
}
