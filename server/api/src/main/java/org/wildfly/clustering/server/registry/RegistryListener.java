/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.registry;

import java.util.Map;

/**
 * Listener for added, updated and removed entries.
 * @author Paul Ferraro
 * @param <K> the registration key
 * @param <V> the registration value
 */
public interface RegistryListener<K, V> {
	/**
	 * Called when new entries have been added.
	 *
	 * @param added a map of entries that have been added
	 */
	void added(Map<K, V> added);

	/**
	 * Called when existing entries have been updated.
	 *
	 * @param updated a map of entries that have been updated
	 */
	void updated(Map<K, V> updated);

	/**
	 * Called when entries have been removed.
	 *
	 * @param removed a map of entries that have been removed
	 */
	void removed(Map<K, V> removed);
}
