/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian.extension;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.kohsuke.MetaInfServices;

/**
 * Arquillian extension for simplifying usage of unmanaged containers.
 * @author Paul Ferraro
 */
@MetaInfServices(LoadableExtension.class)
public class UnmanagedContainerExtension implements LoadableExtension {

	@Override
	public void register(ExtensionBuilder builder) {
		builder.service(ResourceProvider.class, DeploymentContainerRegistryResourceProvider.class);
	}
}
