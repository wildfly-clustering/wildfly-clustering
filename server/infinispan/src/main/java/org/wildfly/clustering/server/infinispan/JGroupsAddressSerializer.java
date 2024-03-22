/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jboss.marshalling.Externalizer;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Formatter;
import org.wildfly.clustering.marshalling.Serializer;
import org.wildfly.clustering.marshalling.jboss.ExternalizerProvider;
import org.wildfly.clustering.marshalling.jboss.SerializerExternalizer;
import org.wildfly.clustering.server.jgroups.AddressSerializer;

/**
 * Serializer for an Infinispan JGroups-based address.
 * @author Paul Ferraro
 */
public enum JGroupsAddressSerializer implements Serializer<JGroupsAddress> {
	INSTANCE;

	@Override
	public void write(DataOutput output, JGroupsAddress address) throws IOException {
		AddressSerializer.INSTANCE.write(output, address.getJGroupsAddress());
	}

	@Override
	public JGroupsAddress read(DataInput input) throws IOException {
		return new JGroupsAddress(AddressSerializer.INSTANCE.read(input));
	}

	@MetaInfServices(ExternalizerProvider.class)
	public static class JGroupsAddressExternalizerProvider implements ExternalizerProvider {
		private final Externalizer externalizer = new SerializerExternalizer(INSTANCE);

		@Override
		public Class<?> getType() {
			return JGroupsAddress.class;
		}

		@Override
		public Externalizer getExternalizer() {
			return this.externalizer;
		}
	}

	@MetaInfServices(Formatter.class)
	public static class JGroupsAddressFormatter extends Formatter.Provided<JGroupsAddress> {
		public JGroupsAddressFormatter() {
			super(Formatter.serialized(JGroupsAddress.class, INSTANCE));
		}
	}
}
