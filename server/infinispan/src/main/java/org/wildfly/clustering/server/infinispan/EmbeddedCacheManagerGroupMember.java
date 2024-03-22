/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.util.NameCache;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerGroupMember implements CacheContainerGroupMember {

	private final JGroupsAddress address;

	public EmbeddedCacheManagerGroupMember(Address address) {
		this((JGroupsAddress) address);
	}

	public EmbeddedCacheManagerGroupMember(JGroupsAddress address) {
		this.address = address;
	}

	@Override
	public JGroupsAddress getAddress() {
		return this.address;
	}

	@Override
	public String getName() {
		return NameCache.get(this.address.getJGroupsAddress());
	}

	@Override
	public int compareTo(GroupMember<Address> member) {
		return this.address.compareTo(member.getAddress());
	}

	@Override
	public int hashCode() {
		return this.address.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof CacheContainerGroupMember)) return false;
		CacheContainerGroupMember member = (CacheContainerGroupMember) object;
		return this.address.equals(member.getAddress());
	}

	@Override
	public String toString() {
		return this.getName();
	}
}
