/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import org.wildfly.clustering.marshalling.protostream.util.StringKeyMapEntry;

/**
 * A session affinity registry entry.
 * @author Paul Ferraro
 * @deprecated Use {@link StringKeyMapEntry} instead.
 */
@Deprecated(forRemoval = true)
public class SessionAffinityRegistryEntry extends StringKeyMapEntry<Void> {
	private static final long serialVersionUID = 1170039258842435000L;

	/**
	 * Creates a registry entry using the specified key.
	 * @param key a registry entry key.
	 */
	public SessionAffinityRegistryEntry(String key) {
		super(key, null);
	}
}
