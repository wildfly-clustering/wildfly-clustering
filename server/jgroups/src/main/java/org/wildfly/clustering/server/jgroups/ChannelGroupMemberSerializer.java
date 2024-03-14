/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.jgroups;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Formatter;
import org.wildfly.clustering.marshalling.Serializer;

/**
 * Marshalling externalizer for an {@link JChannelGroupMember}.
 * @author Paul Ferraro
 */
public enum ChannelGroupMemberSerializer implements Serializer<JChannelGroupMember> {
	INSTANCE;

	@Override
	public void write(DataOutput output, JChannelGroupMember member) throws IOException {
		AddressSerializer.INSTANCE.write(output, member.getAddress());
	}

	@Override
	public JChannelGroupMember read(DataInput input) throws IOException {
		return new JChannelGroupMember(AddressSerializer.INSTANCE.read(input));
	}

	@MetaInfServices(Formatter.class)
	public static class AddressGroupMemberFormatter extends Formatter.Provided<JChannelGroupMember> {
		public AddressGroupMemberFormatter() {
			super(Formatter.serialized(JChannelGroupMember.class, INSTANCE));
		}
	}
}
