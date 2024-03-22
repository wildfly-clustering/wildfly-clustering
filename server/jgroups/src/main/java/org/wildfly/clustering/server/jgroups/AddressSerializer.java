/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jboss.marshalling.Externalizer;
import org.jgroups.Address;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.UUID;
import org.jgroups.util.Util;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Serializer;
import org.wildfly.clustering.marshalling.jboss.ExternalizerProvider;
import org.wildfly.clustering.marshalling.jboss.SerializerExternalizer;

/**
 * Serializer for a JGroups {@link Address}.
 * @author Paul Ferraro
 */
public enum AddressSerializer implements Serializer<Address> {
	INSTANCE;

	@Override
	public void write(DataOutput output, Address address) throws IOException {
		Util.writeAddress(address, output);
	}

	@Override
	public Address read(DataInput input) throws IOException {
		try {
			return Util.readAddress(input);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	static class AddressExternalizerProvider<A extends Address> implements ExternalizerProvider {
		private static final Externalizer EXTERNALIZER = new SerializerExternalizer(INSTANCE);
		private final Class<?> type;

		AddressExternalizerProvider(Class<A> type) {
			this.type = type;
		}

		@Override
		public Class<?> getType() {
			return this.type;
		}

		@Override
		public Externalizer getExternalizer() {
			return EXTERNALIZER;
		}
	}

	@MetaInfServices(ExternalizerProvider.class)
	public static class UUIDExternalizerProvider extends AddressExternalizerProvider<UUID> {
		public UUIDExternalizerProvider() {
			super(UUID.class);
		}
	}

	@MetaInfServices(ExternalizerProvider.class)
	public static class IpAddressExternalizerProvider extends AddressExternalizerProvider<IpAddress> {
		public IpAddressExternalizerProvider() {
			super(IpAddress.class);
		}
	}
}
