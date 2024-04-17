/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.OptionalInt;

import org.jboss.marshalling.Externalizer;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Serializer;
import org.wildfly.clustering.marshalling.jboss.ExternalizerProvider;
import org.wildfly.clustering.marshalling.jboss.SerializerExternalizer;

/**
 * @author Paul Ferraro
 */
public enum JChannelGroupMemberSerializer implements Serializer<JChannelGroupMember> {
	INSTANCE;

	private final Serializer<JChannelGroupMember> serializer = AddressSerializer.INSTANCE.wrap(JChannelGroupMember::getAddress, JChannelGroupMember::new);

	@Override
	public void write(DataOutput output, JChannelGroupMember value) throws IOException {
		this.serializer.write(output, value);
	}

	@Override
	public JChannelGroupMember read(DataInput input) throws IOException {
		return this.serializer.read(input);
	}

	@Override
	public OptionalInt size(JChannelGroupMember object) {
		return this.serializer.size(object);
	}

	@MetaInfServices(ExternalizerProvider.class)
	public static class JChannelGroupMemberExternalizerProvider implements ExternalizerProvider {
		private final Externalizer externalizer = new SerializerExternalizer(INSTANCE);

		@Override
		public Class<?> getType() {
			return JChannelGroupMember.class;
		}

		@Override
		public Externalizer getExternalizer() {
			return this.externalizer;
		}
	}
}
