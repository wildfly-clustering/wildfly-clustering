/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.Container;
import org.testcontainers.lifecycle.Startable;
import org.wildfly.clustering.function.Consumer;

/**
 * Generic JUnit extension for managing the lifecycle of a container.
 * @param <C> the container type
 * @author Paul Ferraro
 */
public class ContainerExtension<C extends Container<C> & Startable> implements AfterAllCallback, BeforeAllCallback, ContainerProvider<C> {
	protected static final System.Logger LOGGER = System.getLogger(ContainerExtension.class.getName());

	private final Function<ExtensionContext, C> factory;
	private final AtomicReference<C> container = new AtomicReference<>();

	public ContainerExtension(Function<ExtensionContext, C> factory) {
		this.factory = factory;
	}

	@Override
	public C getContainer() {
		return this.container.get();
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		C container = this.factory.apply(context);
		Optional.ofNullable(this.container.getAndSet(container)).ifPresent(Consumer.close());
		LOGGER.log(System.Logger.Level.INFO, "Starting {0}", container.getDockerImageName());
		Instant start = Instant.now();
		container.start();
		LOGGER.log(System.Logger.Level.INFO, "Started {0} in {1}", container.getDockerImageName(), Duration.between(start, Instant.now()));
	}

	@Override
	public void afterAll(ExtensionContext context) {
		try (C container = this.container.getAndSet(null)) {
			if (container != null) {
				LOGGER.log(System.Logger.Level.INFO, "Stopping {0}", container.getDockerImageName());
				Instant start = Instant.now();
				container.stop();
				LOGGER.log(System.Logger.Level.INFO, "Stopped {0} in {1}", container.getDockerImageName(), Duration.between(start, Instant.now()));
			}
		}
	}
}
