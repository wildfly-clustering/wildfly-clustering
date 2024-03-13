/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.manager;

/**
 * Records some other object.
 * @param <T> the recorded object type
 * @author Paul Ferraro
 */
public interface Recordable<T> {
	/**
	 * Records the specified object
	 * @param object an object to record
	 */
	void record(T object);

	/**
	 * Resets any previously recorded objects
	 */
	void reset();
}
