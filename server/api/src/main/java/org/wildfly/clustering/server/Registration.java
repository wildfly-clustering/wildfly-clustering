/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server;

/**
 * Encapsulates a registration.
 * @author Paul Ferraro
 */
public interface Registration extends AutoCloseable {
	/** An empty registration */
	Registration EMPTY = () -> {};

	/**
	 * Removes this registration from the associated {@link Registrar}, after which this object is no longer functional.
	 */
	@Override
	void close();
}
