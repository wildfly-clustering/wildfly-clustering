/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.context;

/**
 * Factory for creating a {@link Contextualizer} for a {@link ClassLoader}.
 * @author Paul Ferraro
 */
public interface ContextualizerFactory {
	/**
	 * Creates a {@link Contextualizer} for the specified {@link ClassLoader}.
	 * @param loader a class loader
	 * @return a contextualizer
	 */
	Contextualizer createContextualizer(ClassLoader loader);
}
