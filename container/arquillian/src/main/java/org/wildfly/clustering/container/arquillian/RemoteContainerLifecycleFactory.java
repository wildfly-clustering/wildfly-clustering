/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container.arquillian;

import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.container.spi.Container;
import org.testcontainers.containers.Network;
import org.wildfly.clustering.arquillian.Lifecycle;
import org.wildfly.clustering.arquillian.LifecycleFactory;
import org.wildfly.clustering.container.DefaultContainer;

/**
 * An OCI-capable lifecycle factory.
 * @author Paul Ferraro
 */
public class RemoteContainerLifecycleFactory implements LifecycleFactory {
	private final Network network;

	RemoteContainerLifecycleFactory(Network network) {
		this.network = network;
	}

	@SuppressWarnings("resource")
	@Override
	public Lifecycle createContainerLifecycle(Container<?> container) {
		Lifecycle lifecycle = LifecycleFactory.DEFAULT.createContainerLifecycle(container);
		ContainerDef configuration = container.getContainerConfiguration();
		ExtensionDef extension = configuration.getExtensions().stream().filter(ext -> ext.getExtensionName().equals("OCI")).findAny().orElse(null);
		return (extension != null) ? Lifecycle.composite(List.of(new RemoteContainerLifecycle(new DefaultContainer(container.getName(), extension.getExtensionProperties(), configuration::getContainerProperty).withNetwork(this.network)), lifecycle)) : lifecycle;
	}
}
