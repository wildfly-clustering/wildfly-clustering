/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wildfly.clustering.arquillian.Deployment;
import org.wildfly.clustering.arquillian.DeploymentContainer;
import org.wildfly.clustering.arquillian.DeploymentContainerRegistry;
import org.wildfly.clustering.arquillian.Lifecycle;

/**
 * Abstract container integration test.
 * @author Paul Ferraro
 */
public abstract class AbstractSessionManagerITCase implements Consumer<Archive<?>>, SessionManagementTesterConfiguration, Supplier<WebArchive> {

	@RegisterExtension
	static final ArquillianExtension ARQUILLIAN = new ArquillianExtension();

	@ArquillianResource
	private DeploymentContainerRegistry registry;

	private final Set<String> containerNames;

	/**
	 * Configures test to use all containers in registry
	 */
	protected AbstractSessionManagerITCase() {
		this.containerNames = Set.of();
	}

	/**
	 * Configures test to use selected containers
	 * @param containerNames a set of container names
	 */
	protected AbstractSessionManagerITCase(Set<String> containerNames) {
		this.containerNames = containerNames;
	}

	public WebArchive get() {
		return ShrinkWrap.create(WebArchive.class, this.getClass().getSimpleName() + ".war")
				.addClass(SessionManagementEndpointConfiguration.class)
				.addPackage(this.getEndpointClass().getPackage())
				;
	}

	@Override
	public void accept(Archive<?> archive) {
		Collection<DeploymentContainer> containers = this.getContainers();
		// Deploy archive to all containers
		List<Deployment> deployments = containers.stream().map(container -> container.deploy(archive)).toList();

		try (Lifecycle composite = Lifecycle.composite(deployments)) {
			try (ClientTester tester = this.createClientTester()) {
				tester.test(deployments);
			}
		}
	}

	private Collection<DeploymentContainer> getContainers() {
		if (this.containerNames.isEmpty()) return this.registry.getContainers();

		Collection<DeploymentContainer> containers = new ArrayList<>(this.registry.getContainers().size());
		for (String containerName : this.containerNames) {
			DeploymentContainer container = this.registry.getContainer(containerName);
			if (container == null) {
				throw new IllegalArgumentException(containerName);
			}
			containers.add(container);
		}
		return containers;
	}
}
