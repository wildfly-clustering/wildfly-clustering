/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.net.URI;

/**
 * @author Paul Ferraro
 */
public interface Deployment extends Lifecycle {

	/**
	 * Returns the container associated with this deployment.
	 * @return
	 */
	DeploymentContainer getContainer();

	/**
	 * Locates the base URI of the specific resource class.
	 * For a servlet class, this would return the URI containing the context path of the associated ServletContext.
	 * @param resourceClass
	 * @return
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
}
