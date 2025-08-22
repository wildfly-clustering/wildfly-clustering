/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.Serializable;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * @author Paul Ferraro
 */
public class LocalEmbeddedCacheManagerGroupMember implements CacheContainerGroupMember, Serializable {
	private static final long serialVersionUID = -8987757972156115087L;

	private final String name;

	public LocalEmbeddedCacheManagerGroupMember(String name) {
		this.name = name;
	}

	@Override
	public Address getAddress() {
		return LocalModeAddress.INSTANCE;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int compareTo(GroupMember<Address> member) {
		return this.name.compareTo(member.getName());
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof GroupMember member)) return false;
		return this.name.equals(member.getName());
	}

	@Override
	public String toString() {
		return this.name;
	}
}
