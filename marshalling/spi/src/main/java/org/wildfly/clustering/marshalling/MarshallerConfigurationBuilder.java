/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

/**
 * @author Paul Ferraro
 * @param <C> the configuration type
 * @param <E> the marshalling configuration entry type
 * @param <B> the configuration builder type
 */
public interface MarshallerConfigurationBuilder<C, E, B extends MarshallerConfigurationBuilder<C, E, B>> {

	/**
	 * Registers configuration entry.
	 * @param entry a configuration entry
	 * @return a reference to this builder
	 */
	B register(E entry);

	/**
	 * Loads marshalling configuration from the specified class loader.
	 * @param loader a class loader
	 * @return a reference to this builder
	 */
	B load(ClassLoader loader);

	/**
	 * Builds the marshalling configuration.
	 * @return a marshalling configuration.
	 */
	C build();
}
