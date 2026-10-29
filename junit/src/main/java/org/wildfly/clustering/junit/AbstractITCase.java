/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wildfly.clustering.arquillian.Deployment;
import org.wildfly.clustering.arquillian.DeploymentContainer;
import org.wildfly.clustering.arquillian.DeploymentContainerRegistry;
import org.wildfly.clustering.arquillian.Lifecycle;

/**
 * An abstract integration test that deploys to a set of containers.
 * @author Paul Ferraro
 * @param <C> the test configuration type
 * @param <A> the archive type
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractITCase<C, A extends Archive<A>> implements Consumer<C>, ApplicationConfiguration<C, A>, DeploymentContainerConfiguration {
	@RegisterExtension
	private static final ArquillianExtension ARQUILLIAN = new ArquillianExtension();

	@ArquillianResource
	private DeploymentContainerRegistry registry;

	// Workaround for https://github.com/arquillian/arquillian-core/issues/930
	private final AtomicBoolean beforeAll = new AtomicBoolean(true);
	private final Supplier<Tester> testerFactory;

	/**
	 * Constructs a new integration test using the specified tester factory and configuration.
	 * @param testerFactory a tester factory
	 */
	protected AbstractITCase(Supplier<Tester> testerFactory) {
		this.testerFactory = testerFactory;
	}

	@BeforeEach
	void init() {
		// Workaround for https://github.com/arquillian/arquillian-core/issues/930
		if (this.beforeAll.getAndSet(false)) {
			this.getDeploymentContainers().forEach(DeploymentContainer::start);
		}
	}

	@AfterAll
	void destroy() {
		this.getDeploymentContainers().forEach(DeploymentContainer::close);
	}

	@Override
	public DeploymentContainerRegistry getDeploymentContainerRegistry() {
		return this.registry;
	}

	@Override
	public void accept(C configuration) {
		Archive<?> archive = this.createArchive(configuration);
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
