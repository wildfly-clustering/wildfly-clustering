/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.util.Optional;

import org.jgroups.Address;
import org.jgroups.util.NameCache;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * @author Paul Ferraro
 */
public class JChannelGroupMember implements ChannelGroupMember {

	private final Address address;

	public JChannelGroupMember(Address address) {
		this.address = address;
	}

	@Override
	public String getName() {
		return NameCache.get(this.address);
	}

	@Override
	public Address getAddress() {
		return this.address;
	}

	@Override
	public int hashCode() {
		return this.address.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof ChannelGroupMember)) return false;
		ChannelGroupMember member = (ChannelGroupMember) object;
		return this.address.equals(member.getAddress());
	}

	@Override
	public String toString() {
		return Optional.ofNullable(this.getName()).orElseGet(this.address::toString);
	}

	@Override
	public int compareTo(GroupMember<Address> member) {
		return this.address.compareTo(member.getAddress());
	}
}
