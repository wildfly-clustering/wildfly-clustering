/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.net.URI;

/**
 * Encapsulates an Arquillian deployment.
 * @author Paul Ferraro
 */
public interface Deployment extends Lifecycle, Comparable<Deployment> {
	/**
	 * Returns the name of this deployment
	 * @return the deployment name
	 */
	String getName();

	/**
	 * Returns the container associated with this deployment.
	 * @return the container associated with this deployment.
	 */
	DeploymentContainer getContainer();

	/**
	 * Locates the base URI of the specific resource class.
	 * For a servlet class, this would return the URI containing the context path of the associated ServletContext.
	 * @param resourceClass the class of an arquillian resource
	 * @return the base URI of the specific resource class.
	 */
	default URI locate(Class<?> resourceClass) {
		return this.locate(resourceClass.getName());
	}

	/**
	 * Locates the base URI of the specific resource.
	 * @param resourceName the name of a resource in the deployment
	 * @return a URI to be used to invoke the specified resource.
	 */
	URI locate(String resourceName);

	@Override
	default int compareTo(Deployment deployment) {
		int result = this.getContainer().compareTo(deployment.getContainer());
		return (result == 0) ? this.getName().compareTo(deployment.getName()) : result;
	}
}
