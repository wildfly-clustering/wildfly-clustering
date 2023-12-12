/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.util.NameCache;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerGroupMember implements CacheContainerGroupMember, Serializable {
	private static final long serialVersionUID = 461573336820082207L;

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

	@SuppressWarnings("unused")
	Object writeReplace() throws ObjectStreamException {
		return new JChannelGroupMemberProxy(this.address);
	}

	static class JChannelGroupMemberProxy implements Serializable {
		private static final long serialVersionUID = 1154615703830003932L;

		private transient JGroupsAddress address;

		JChannelGroupMemberProxy(JGroupsAddress address) {
			this.address = address;
		}

		private void writeObject(ObjectOutputStream output) throws IOException {
			output.defaultWriteObject();
			JGroupsAddressSerializer.INSTANCE.write(output, this.address);
		}

		private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
			input.defaultReadObject();
			this.address = JGroupsAddressSerializer.INSTANCE.read(input);
		}

		@SuppressWarnings("unused")
		Object readResolve() throws ObjectStreamException {
			return new EmbeddedCacheManagerGroupMember(this.address);
		}
	}
}
