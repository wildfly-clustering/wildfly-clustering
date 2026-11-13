/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * Provider of the {@link DeploymentContainerRegistry} resource.
 * @author Paul Ferraro
 */
public class DeploymentContainerRegistryResourceProvider implements ResourceProvider {

	@Inject
	private Instance<CloseableDeploymentContainerRegistry> registry;

	/** Default constructor */
	public DeploymentContainerRegistryResourceProvider() {
		// Do nothing
	}

	@Override
	public boolean canProvide(Class<?> type) {
		return type.isAssignableFrom(DeploymentContainerRegistry.class);
	}

	@Override
	public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
		return this.registry.get();
	}
}
