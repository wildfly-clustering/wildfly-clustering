/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.util.NameCache;
import org.wildfly.clustering.server.group.GroupMember;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;

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
		return this.address.getJGroupsAddress().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof CacheContainerGroupMember) {
			CacheContainerGroupMember member = (CacheContainerGroupMember) object;
			return this.address.equals(member.getAddress());
		}
		if (object instanceof ChannelGroupMember) {
			ChannelGroupMember member = (ChannelGroupMember) object;
			return this.address.getJGroupsAddress().equals(member.getAddress());
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getName();
	}
}
