/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.Serializable;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.wildfly.clustering.server.group.GroupMember;
import org.wildfly.clustering.server.local.LocalGroupMember;

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
		if (member instanceof LocalEmbeddedCacheManagerGroupMember) {
			LocalEmbeddedCacheManagerGroupMember localMember = (LocalEmbeddedCacheManagerGroupMember) member;
			return this.name.compareTo(localMember.name);
		}
		return this.getAddress().compareTo(member.getAddress());
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof LocalEmbeddedCacheManagerGroupMember) && !(object instanceof LocalGroupMember)) return false;
		org.wildfly.clustering.server.GroupMember member = (org.wildfly.clustering.server.GroupMember) object;
		return this.name.equals(member.getName());
	}

	@Override
	public String toString() {
		return this.name;
	}
}
