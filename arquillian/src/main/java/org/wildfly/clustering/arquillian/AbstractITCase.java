/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.util.List;
import java.util.function.Function;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;

/**
 * An abstract integration test that runs against a specific set of containers.
 * @author Paul Ferraro
 * @param <A> the archive type
 * @param <C> the tester configuration type
 */
public abstract class AbstractITCase<C, A extends Archive<A>> implements Runnable, ApplicationConfiguration<C, A>, DeploymentContainerConfiguration {

	@ArquillianResource
	private DeploymentContainerRegistry registry;

	@Override
	public DeploymentContainerRegistry getDeploymentContainerRegistry() {
		return this.registry;
	}

	private final Function<C, Tester> testerFactory;
	private final C configuration;

	protected AbstractITCase(Function<C, Tester> testerFactory, C configuration) {
		this.testerFactory = testerFactory;
		this.configuration = configuration;
	}

	@Override
	public void run() {
		Archive<?> archive = this.createArchive(this.configuration);
		List<Deployment> deployments = this.getDeploymentContainers().stream().map(container -> container.deploy(archive)).toList();
		try (Lifecycle composite = Lifecycle.composite(deployments)) {
			try (Tester tester = this.testerFactory.apply(this.configuration)) {
				tester.accept(deployments);
			}
		}
	}
}
