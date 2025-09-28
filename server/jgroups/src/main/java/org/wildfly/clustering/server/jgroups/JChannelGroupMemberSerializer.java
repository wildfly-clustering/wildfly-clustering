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
 * A serializer for a channel-based group member.
 * @author Paul Ferraro
 */
public enum JChannelGroupMemberSerializer implements Serializer<JChannelGroupMember> {
	/** Singleton instance */
	INSTANCE;

	private final Serializer<JChannelGroupMember> serializer = AddressSerializer.INSTANCE.wrap(JChannelGroupMember::getId, JChannelGroupMember::new);

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

	/**
	 * Provides an externalizer for a {@link JChannelGroupMember}.
	 */
	@MetaInfServices(ExternalizerProvider.class)
	public static class JChannelGroupMemberExternalizerProvider implements ExternalizerProvider {
		private final Externalizer externalizer = new SerializerExternalizer(INSTANCE);

		/**
		 * Creates a provider of an externalizer for a {@link JChannelGroupMember}.
		 */
		public JChannelGroupMemberExternalizerProvider() {
		}

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
