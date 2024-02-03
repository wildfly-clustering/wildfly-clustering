/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.Container;
import org.testcontainers.lifecycle.Startable;

/**
 * Generic JUnit extension for managing the lifecycle of a container.
 * @author Paul Ferraro
 */
public class ContainerExtension<C extends Container<C> & Startable> implements AfterAllCallback, BeforeAllCallback, ContainerProvider<C> {
	protected static final Logger LOGGER = Logger.getLogger(ContainerExtension.class);

	private final Function<ExtensionContext, C> factory;
	private C container;

	public ContainerExtension(Function<ExtensionContext, C> factory) {
		this.factory = factory;
	}

	@Override
	public C getContainer() {
		return this.container;
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		this.container = this.factory.apply(context);
		LOGGER.infof("Starting %s", this.container.getDockerImageName());
		Instant start = Instant.now();
		this.container.start();
		LOGGER.infof("Started %s in %s", this.container.getDockerImageName(), Duration.between(start, Instant.now()));
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		if (this.container != null) {
			LOGGER.infof("Starting %s", this.container.getDockerImageName());
			Instant start = Instant.now();
			this.container.stop();
			LOGGER.infof("Stopped %s in %s", this.container.getDockerImageName(), Duration.between(start, Instant.now()));
		}
	}
}
