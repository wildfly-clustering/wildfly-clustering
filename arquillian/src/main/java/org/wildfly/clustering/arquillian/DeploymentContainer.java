/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import org.jboss.shrinkwrap.api.Archive;

/**
 * Encapsulates an Arquillian deployment container.
 * @author Paul Ferraro
 */
public interface DeploymentContainer extends Lifecycle, Comparable<DeploymentContainer> {

	/**
	 * Returns the name of this container.
	 * @return the container name
	 */
	String getName();

	/**
	 * Deploys the specified archive to this container.
	 * @param archive the archive to deploy
	 * @return the deployment of the specified archive.
	 */
	Deployment deploy(Archive<?> archive);

	@Override
	default int compareTo(DeploymentContainer container) {
		return this.getName().compareTo(container.getName());
	}
}
