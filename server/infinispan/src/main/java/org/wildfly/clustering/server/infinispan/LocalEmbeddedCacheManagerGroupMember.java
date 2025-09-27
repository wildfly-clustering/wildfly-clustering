/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.Serializable;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.group.AbstractGroupMember;

/**
 * @author Paul Ferraro
 */
public class LocalEmbeddedCacheManagerGroupMember extends AbstractGroupMember<Address> implements CacheContainerGroupMember, Serializable {
	private static final long serialVersionUID = -8987757972156115087L;

	private final String name;

	public LocalEmbeddedCacheManagerGroupMember(String name) {
		this.name = name;
	}

	@Override
	public Address getId() {
		return LocalModeAddress.INSTANCE;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return (object instanceof GroupMember member) && this.getName().equals(member.getName());
	}
}
