/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.manager;

import java.util.function.Supplier;

/**
 * Configuration of a manager of distributed state.
 * @param <I> the identifier type
 * @author Paul Ferraro
 */
public interface ManagerConfiguration<I> {

	/**
	 * Returns a factory for creating identifiers for use by this manager.
	 * @return an identifier factory
	 */
	Supplier<I> getIdentifierFactory();
}
