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
	/** Singleton instance */
	INSTANCE;

	@Override
	public Address read(DataInput input) throws IOException {
		try {
			return Util.readAddress(input);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void write(DataOutput output, Address address) throws IOException {
		Util.writeAddress(address, output);
	}

	@Override
	public OptionalInt size(Address address) {
		return OptionalInt.of(Util.size(address));
	}

	/**
	 * Provides an externalizer of a JGroups address.
	 * @param <A> the address type
	 */
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

	/**
	 * Provider of a {@link UUID} externalizer.
	 */
	@MetaInfServices(ExternalizerProvider.class)
	public static class UUIDExternalizerProvider extends AddressExternalizerProvider<UUID> {
		/**
		 * Creates a provider of a {@link UUID} externalizer.
		 */
		public UUIDExternalizerProvider() {
			super(UUID.class);
		}
	}

	/**
	 * Provider of a {@link IpAddress} externalizer.
	 */
	@MetaInfServices(ExternalizerProvider.class)
	public static class IpAddressExternalizerProvider extends AddressExternalizerProvider<IpAddress> {
		/**
		 * Creates a provider of a {@link IpAddress} externalizer.
		 */
		public IpAddressExternalizerProvider() {
			super(IpAddress.class);
		}
	}
}
