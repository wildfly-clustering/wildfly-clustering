/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.Container;
import org.testcontainers.lifecycle.Startable;

/**
 * Generic JUnit extension for managing the lifecycle of a container.
 * @param <C> the container type
 * @author Paul Ferraro
 */
public class ContainerExtension<C extends Container<C> & Startable> implements AfterAllCallback, BeforeAllCallback, ContainerProvider<C> {
	protected static final System.Logger LOGGER = System.getLogger(ContainerExtension.class.getName());

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
		LOGGER.log(System.Logger.Level.INFO, "Starting {0}", this.container.getDockerImageName());
		Instant start = Instant.now();
		this.container.start();
		LOGGER.log(System.Logger.Level.INFO, "Started {0} in {1}", this.container.getDockerImageName(), Duration.between(start, Instant.now()));
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		if (this.container != null) {
			LOGGER.log(System.Logger.Level.INFO, "Stopping {0}", this.container.getDockerImageName());
			Instant start = Instant.now();
			this.container.stop();
			LOGGER.log(System.Logger.Level.INFO, "Stopped {0} in {1}", this.container.getDockerImageName(), Duration.between(start, Instant.now()));
		}
	}
}
