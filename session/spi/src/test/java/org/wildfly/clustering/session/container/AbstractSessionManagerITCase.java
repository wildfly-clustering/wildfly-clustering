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

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wildfly.clustering.arquillian.Deployment;
import org.wildfly.clustering.arquillian.DeploymentContainer;
import org.wildfly.clustering.arquillian.DeploymentContainerRegistry;

/**
 * Abstract container integration test.
 * @author Paul Ferraro
 */
public abstract class AbstractSessionManagerITCase implements Consumer<Archive<?>>, SessionManagementTesterConfiguration {

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

	@Override
	public void accept(Archive<?> archive) {
		Collection<DeploymentContainer> containers = this.getContainers();
		List<Deployment> deployments = new ArrayList<>(containers.size());

		try (ClientTester tester = new SessionManagementTester(this)) {

			for (DeploymentContainer container : containers) {
				deployments.add(container.deploy(archive));
			}

			tester.test(deployments);
		} finally {
			for (Deployment deployment : deployments) {
				deployment.close();
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
