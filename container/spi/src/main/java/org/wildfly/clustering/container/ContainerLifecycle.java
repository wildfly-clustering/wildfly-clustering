/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.testcontainers.lifecycle.Startable;
import org.wildfly.clustering.function.UnaryOperator;

/**
 * Encapsulates the lifecycle of a container.
 * @author Paul Ferraro
 */
public interface ContainerLifecycle extends Startable {
	/**
	 * An empty container lifecycle.
	 */
	ContainerLifecycle EMPTY = new ContainerLifecycle() {
		@Override
		public boolean isStarted() {
			return false;
		}

		@Override
		public void start() {
		}

		@Override
		public void stop() {
		}
	};

	/**
	 * Returns true if this container is started, false otherwise.
	 * @return true if this container is started, false otherwise.
	 */
	boolean isStarted();

	/**
	 * Returns a container lifecycle configured from the specified properties.
	 * @param properties the properties from which to configure a container lifecycle
	 * @return a container lifecycle configured from the specified properties.
	 */
	static ContainerLifecycle from(Properties properties) {
		String image = properties.getProperty(DefaultContainer.IMAGE_PROPERTY);
		if (image == null) return EMPTY;
		Map<String, String> containerProperties = new TreeMap<>();
		for (String propertyName : properties.stringPropertyNames()) {
			if (propertyName.startsWith(DefaultContainer.OCI_PROPERTY_PREFIX) || propertyName.startsWith(DefaultContainer.ENV_PROPERTY_PREFIX) || propertyName.startsWith(DefaultContainer.FILE_PROPERTY_PREFIX)) {
				containerProperties.put(propertyName, properties.getProperty(propertyName));
			}
		}
		return new DefaultContainer(image, containerProperties, UnaryOperator.identity());
	}
}
