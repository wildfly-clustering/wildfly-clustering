/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container.arquillian;

import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.container.spi.Container;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.arquillian.Lifecycle;
import org.wildfly.clustering.arquillian.LifecycleFactory;
import org.wildfly.clustering.container.DefaultContainer;

/**
 * An OCI-capable lifecycle factory.
 * @author Paul Ferraro
 */
@MetaInfServices(LifecycleFactory.class)
public class RemoteContainerLifecycleFactory implements LifecycleFactory {

	/**
	 * Constructs a new remote container lifecycle factory.
	 */
	public RemoteContainerLifecycleFactory() {
		// For use by service loader
	}

	@Override
	public Lifecycle createContainerLifecycle(Container<?> container) {
		ContainerDef configuration = container.getContainerConfiguration();
		ExtensionDef extension = configuration.getExtensions().stream().filter(ext -> ext.getExtensionName().equals("OCI")).findAny().orElse(null);
		Lifecycle lifecycle = LifecycleFactory.DEFAULT.createContainerLifecycle(container);
		return (extension != null) ? Lifecycle.composite(List.of(new RemoteContainerLifecycle(new DefaultContainer(container.getName(), extension.getExtensionProperties(), configuration::getContainerProperty)), lifecycle)) : lifecycle;
	}
}
