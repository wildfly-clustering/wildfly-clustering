/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;

/**
 * An abstract integration test that runs against a specific set of containers.
 * @author Paul Ferraro
 * @param <C> the test configuration type
 * @param <A> the archive type
 */
public abstract class AbstractITCase<C, A extends Archive<A>> implements Consumer<C>, ApplicationConfiguration<C, A>, DeploymentContainerConfiguration {

	@ArquillianResource
	private DeploymentContainerRegistry registry;

	private final Supplier<Tester> testerFactory;

	/**
	 * Constructs a new integration test using the specified tester factory and configuration.
	 * @param testerFactory a tester factory
	 */
	protected AbstractITCase(Supplier<Tester> testerFactory) {
		this.testerFactory = testerFactory;
	}

	@Override
	public DeploymentContainerRegistry getDeploymentContainerRegistry() {
		return this.registry;
	}

	@Override
	public void accept(C configuration) {
		Archive<?> archive = this.createArchive(configuration);
		try (Lifecycle manualLifecycle = Lifecycle.composite(this.getDeploymentContainers().stream().filter(Predicate.not(DeploymentContainer::isStarted)).toList())) {
			manualLifecycle.start();
			List<DeploymentContainer> containers = this.getDeploymentContainers();
			List<Deployment> deployments = new ArrayList<>(containers.size());
			try (Lifecycle deploymentLifecycle = Lifecycle.composite(deployments)) {
				for (DeploymentContainer container : containers) {
					deployments.add(container.deploy(archive));
				}
				try (Tester tester = this.testerFactory.get()) {
					tester.accept(deployments);
				}
			}
		}
	}
}
