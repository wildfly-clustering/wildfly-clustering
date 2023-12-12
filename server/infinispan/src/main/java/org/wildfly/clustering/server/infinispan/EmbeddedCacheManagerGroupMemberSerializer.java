/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.BinaryFormatter;
import org.wildfly.clustering.marshalling.Formatter;
import org.wildfly.clustering.marshalling.Serializer;

/**
 * Marshalling externalizer for an {@link EmbeddedCacheManagerGroupMember}.
 * @author Paul Ferraro
 */
public enum EmbeddedCacheManagerGroupMemberSerializer implements Serializer<EmbeddedCacheManagerGroupMember> {
	INSTANCE;

	@Override
	public void write(DataOutput output, EmbeddedCacheManagerGroupMember member) throws IOException {
		JGroupsAddressSerializer.INSTANCE.write(output, member.getAddress());
	}

	@Override
	public EmbeddedCacheManagerGroupMember read(DataInput input) throws IOException {
		return new EmbeddedCacheManagerGroupMember(JGroupsAddressSerializer.INSTANCE.read(input));
	}

	@MetaInfServices(Formatter.class)
	public static class AddressGroupMemberFormatter extends BinaryFormatter<EmbeddedCacheManagerGroupMember> {
		public AddressGroupMemberFormatter() {
			super(EmbeddedCacheManagerGroupMember.class, INSTANCE);
		}
	}
}
