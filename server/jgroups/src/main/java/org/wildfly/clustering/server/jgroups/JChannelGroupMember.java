/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.jgroups.Address;
import org.jgroups.util.NameCache;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * @author Paul Ferraro
 */
public class JChannelGroupMember implements ChannelGroupMember, Serializable {
	private static final long serialVersionUID = 3818614808896402906L;

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
		return this.getName();
	}

	@Override
	public int compareTo(GroupMember<Address> member) {
		return this.address.compareTo(member.getAddress());
	}

	@SuppressWarnings("unused")
	Object writeReplace() throws ObjectStreamException {
		return new JChannelGroupMemberProxy(this.address);
	}

	static class JChannelGroupMemberProxy implements Serializable {
		private static final long serialVersionUID = 1154615703830003932L;

		private transient Address address;

		JChannelGroupMemberProxy(Address address) {
			this.address = address;
		}

		private void writeObject(ObjectOutputStream output) throws IOException {
			output.defaultWriteObject();
			AddressSerializer.INSTANCE.write(output, this.address);
		}

		private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
			input.defaultReadObject();
			this.address = AddressSerializer.INSTANCE.read(input);
		}

		@SuppressWarnings("unused")
		Object readResolve() throws ObjectStreamException {
			return new JChannelGroupMember(this.address);
		}
	}
}
