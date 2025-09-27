/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.util.Optional;

import org.jgroups.Address;
import org.jgroups.util.NameCache;
import org.wildfly.clustering.server.group.AbstractGroupMember;

/**
 * @author Paul Ferraro
 */
public class JChannelGroupMember extends AbstractGroupMember<Address> implements ChannelGroupMember {

	private final Address address;

	public JChannelGroupMember(Address address) {
		this.address = address;
	}

	@Override
	public String getName() {
		// Logical name can be null if no longer a member of the view
		return Optional.ofNullable(NameCache.get(this.address)).orElseGet(this.address::toString);
	}

	@Override
	public Address getId() {
		return this.address;
	}
}
