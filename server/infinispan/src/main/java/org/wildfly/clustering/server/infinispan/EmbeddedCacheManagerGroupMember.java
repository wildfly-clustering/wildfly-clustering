/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.util.Optional;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.util.NameCache;
import org.wildfly.clustering.server.group.AbstractGroupMember;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;

/**
 * A group member of an Infinispan cache container.
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerGroupMember extends AbstractGroupMember<Address> implements CacheContainerGroupMember {

	private final JGroupsAddress address;

	/**
	 * Creates a cache container-based group member.
	 * @param address the Infinispan address of the group member
	 */
	public EmbeddedCacheManagerGroupMember(Address address) {
		this((JGroupsAddress) address);
	}

	/**
	 * Creates a cache container-based group member.
	 * @param address the Infinispan address of the group member
	 */
	public EmbeddedCacheManagerGroupMember(JGroupsAddress address) {
		this.address = address;
	}

	@Override
	public JGroupsAddress getId() {
		return this.address;
	}

	@Override
	public String getName() {
		org.jgroups.Address address = this.address.getJGroupsAddress();
		return Optional.ofNullable(NameCache.get(address)).orElseGet(address::toString);
	}

	@Override
	public boolean equals(Object object) {
		return super.equals(object) || ((object instanceof ChannelGroupMember member) && this.address.getJGroupsAddress().equals(member.getId()));
	}
}
