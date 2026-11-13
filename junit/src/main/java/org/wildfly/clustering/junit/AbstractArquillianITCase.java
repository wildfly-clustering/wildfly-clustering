/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.clustering.arquillian.Deployment;
import org.wildfly.clustering.arquillian.DeploymentContainer;
import org.wildfly.clustering.arquillian.DeploymentContainerRegistry;
import org.wildfly.clustering.arquillian.Lifecycle;

/**
 * An abstract integration test that deploys a single archive to a set of arquillian containers.
 * @author Paul Ferraro
 * @param <C> the test configuration type
 * @param <A> the archive type
 */
@ExtendWith(ArquillianExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractArquillianITCase<C, A extends Archive<A>> implements Consumer<C>, ApplicationConfiguration<C, A>, DeploymentContainerConfiguration {
	private final System.Logger logger = System.getLogger(this.getClass().getCanonicalName());

	@ArquillianResource
	private DeploymentContainerRegistry registry;

	private final Retry retry = Retry.of(this.getClass().getName(), RetryConfig.custom().maxAttempts(2).build());
	private final AtomicBoolean beforeAll = new AtomicBoolean(true);
	private final Supplier<Tester> testerFactory;

	/**
	 * Constructs a new integration test using the specified tester factory and configuration.
	 * @param testerFactory a tester factory
	 */
	protected AbstractArquillianITCase(Supplier<Tester> testerFactory) {
		this.testerFactory = testerFactory;
	}

	/**
	 * Initialisation prior to all tests test.
	 */
	@BeforeEach
	void initTest() {
		// Simulate @BeforeAll
		// Workaround for https://github.com/arquillian/arquillian-core/issues/930
		if (this.beforeAll.getAndSet(false)) {
			this.init();
		}
	}

	/**
	 * Initialises/starts containers prior to commencement of all tests.
	 */
	// N.B. Arquillian resource is not resolvable yet!
	// https://github.com/arquillian/arquillian-core/issues/930
	//@org.junit.jupiter.api.BeforeAll
	protected void init() {
		this.getDeploymentContainers().forEach(DeploymentContainer::start);
	}

	/**
	 * Stops/destroys containers following completion of all tests.
	 */
	@AfterAll
	protected void destroy() {
		for (DeploymentContainer container : this.getDeploymentContainers()) {
			try {
				if (container.isStarted()) {
					container.stop();
				}
			} catch (RuntimeException | Error e) {
				this.logger.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
			} finally {
				try {
					container.close();
				} catch (RuntimeException | Error e) {
					this.logger.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
				}
			}
		}
	}

	@Override
	public DeploymentContainerRegistry getDeploymentContainerRegistry() {
		return Objects.requireNonNull(this.registry);
	}

	@Override
	public void accept(C configuration) {
		Archive<?> archive = this.createArchive(configuration);
		// Retry deployment, to handle the case where a cached remote JMX connection is no longer valid
		Function<DeploymentContainer, Deployment> deployer = this.retry.<DeploymentContainer, Deployment>decorateCheckedFunction(container -> container.deploy(archive)).unchecked();
		List<DeploymentContainer> containers = this.getDeploymentContainers();
		List<Deployment> deployments = new ArrayList<>(containers.size());
		try (Lifecycle deploymentLifecycle = Lifecycle.composite(deployments)) {
			for (DeploymentContainer container : containers) {
				deployments.add(deployer.apply(container));
			}
			try (Tester tester = this.testerFactory.get()) {
				tester.accept(deployments);
			}
		}
	}
}
