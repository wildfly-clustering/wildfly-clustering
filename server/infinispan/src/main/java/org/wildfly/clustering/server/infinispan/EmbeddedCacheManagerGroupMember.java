/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.Address;
import org.jgroups.util.NameCache;
import org.wildfly.clustering.server.group.AbstractGroupMember;

/**
 * A group member of an Infinispan cache container.
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerGroupMember extends AbstractGroupMember<Address> implements CacheContainerGroupMember {

	private final Address address;

	/**
	 * Creates a cache container-based group member.
	 * @param address the Infinispan address of the group member
	 */
	public EmbeddedCacheManagerGroupMember(Address address) {
		this.address = address;
	}

	@Override
	public Address getId() {
		return this.address;
	}

	@Override
	public String getName() {
		return NameCache.get(Address.toExtendedUUID(this.address));
	}
}
