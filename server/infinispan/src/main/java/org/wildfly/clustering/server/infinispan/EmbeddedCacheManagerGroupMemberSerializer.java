/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jboss.marshalling.Externalizer;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Formatter;
import org.wildfly.clustering.marshalling.Serializer;
import org.wildfly.clustering.marshalling.jboss.ExternalizerProvider;
import org.wildfly.clustering.marshalling.jboss.SerializerExternalizer;

/**
 * Marshalling externalizer for an {@link EmbeddedCacheManagerGroupMember}.
 * @author Paul Ferraro
 */
public enum EmbeddedCacheManagerGroupMemberSerializer implements Serializer<EmbeddedCacheManagerGroupMember> {
	/** Singleton instance */
	INSTANCE;

	@Override
	public void write(DataOutput output, EmbeddedCacheManagerGroupMember member) throws IOException {
		AddressSerializer.INSTANCE.write(output, member.getId());
	}

	@Override
	public EmbeddedCacheManagerGroupMember read(DataInput input) throws IOException {
		return new EmbeddedCacheManagerGroupMember(AddressSerializer.INSTANCE.read(input));
	}

	/**
	 * Provides an externalizer of a cache container-based group member.
	 */
	@MetaInfServices(ExternalizerProvider.class)
	public static class AddressGroupMemberExternalizerProvider implements ExternalizerProvider {
		private final Externalizer externalizer = new SerializerExternalizer(INSTANCE);

		/**
		 * Creates an externalizer provider for a group member.
		 */
		public AddressGroupMemberExternalizerProvider() {
		}

		@Override
		public Class<?> getType() {
			return EmbeddedCacheManagerGroupMember.class;
		}

		@Override
		public Externalizer getExternalizer() {
			return this.externalizer;
		}
	}

	/**
	 * Formatter of a cache container-based group member.
	 */
	@MetaInfServices(Formatter.class)
	public static class AddressGroupMemberFormatter extends Formatter.Provided<EmbeddedCacheManagerGroupMember> {

		/**
		 * Creates a formatter of a group member.
		 */
		public AddressGroupMemberFormatter() {
			super(INSTANCE.toFormatter(EmbeddedCacheManagerGroupMember.class));
		}
	}
}
