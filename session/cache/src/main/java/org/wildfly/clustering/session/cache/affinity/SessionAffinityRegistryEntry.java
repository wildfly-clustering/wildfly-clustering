/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import java.util.Map;

/**
 * @author Paul Ferraro
 */
public class SessionAffinityRegistryEntry implements Map.Entry<String, Void> {

	private final String key;

	public SessionAffinityRegistryEntry(String key) {
		this.key = key;
	}

	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public Void getValue() {
		return null;
	}

	@Override
	public Void setValue(Void value) {
		return null;
	}

	@Override
	public int hashCode() {
		return this.key.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof SessionAffinityRegistryEntry)) return false;
		SessionAffinityRegistryEntry entry = (SessionAffinityRegistryEntry) object;
		return this.key.equals(entry.key);
	}

	@Override
	public String toString() {
		return this.key;
	}
}
