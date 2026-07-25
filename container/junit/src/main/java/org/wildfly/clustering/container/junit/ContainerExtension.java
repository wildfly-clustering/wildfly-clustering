/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container.junit;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.lifecycle.Startable;
import org.wildfly.clustering.container.ContainerProvider;
import org.wildfly.clustering.function.Consumer;

/**
 * A JUnit extension for managing the lifecycle of an OCI container.
 * @param <C> the container type
 * @author Paul Ferraro
 */
public class ContainerExtension<C extends Startable> implements AfterAllCallback, BeforeAllCallback, ContainerProvider<C> {
	/** A logger for use by subclasses */
	protected static final System.Logger LOGGER = System.getLogger(ContainerExtension.class.getName());

	private static final AtomicInteger COUNTER = new AtomicInteger(0);
	private final Function<ExtensionContext, C> factory;
	private final AtomicReference<C> container = new AtomicReference<>();

	/**
	 * Constructs a JUnit extension that manages the lifecycle of a container.
	 * @param factory a container factory
	 */
	public ContainerExtension(Function<ExtensionContext, C> factory) {
		this.factory = factory;
	}

	@Override
	public C getContainer() {
		return this.container.get();
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		if (COUNTER.getAndIncrement() == 0) {
			C container = this.factory.apply(context);
			Optional.ofNullable(this.container.getAndSet(container)).ifPresent(Consumer.close());
			LOGGER.log(System.Logger.Level.INFO, "Starting {0}", container);
			Instant start = Instant.now();
			container.start();
			LOGGER.log(System.Logger.Level.INFO, "Started {0} in {1}", container, Duration.between(start, Instant.now()));
		}
	}

	@Override
	public void afterAll(ExtensionContext context) {
		if (COUNTER.decrementAndGet() == 0) {
			try (C container = this.container.getAndSet(null)) {
				if (container != null) {
					LOGGER.log(System.Logger.Level.INFO, "Stopping {0}", container);
					Instant start = Instant.now();
					container.stop();
					LOGGER.log(System.Logger.Level.INFO, "Stopped {0} in {1}", container, Duration.between(start, Instant.now()));
				}
			}
		}
	}
}
