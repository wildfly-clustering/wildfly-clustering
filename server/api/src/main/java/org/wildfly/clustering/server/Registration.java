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

	/**
	 * Creates a composite registration.
	 * @param registrations a collection of registrations
	 * @return a composite registration.
	 */
	static Registration composite(Iterable<? extends Registration> registrations) {
		return new Registration() {
			@Override
			public void close() {
				for (Registration registration : registrations) {
					registration.close();
				}
			}
		};
	}
}
