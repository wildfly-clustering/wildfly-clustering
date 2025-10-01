/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.OptionalInt;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.jboss.marshalling.Externalizer;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Formatter;
import org.wildfly.clustering.marshalling.Serializer;
import org.wildfly.clustering.marshalling.jboss.ExternalizerProvider;
import org.wildfly.clustering.marshalling.jboss.SerializerExternalizer;

/**
 * Serializer of local Infinispan address.
 * @author Paul Ferraro
 */
public enum LocalAddressSerializer implements Serializer<Address> {
	/** Singleton instance */
	INSTANCE;

	private final Serializer<Address> serializer = Serializer.of(LocalModeAddress.INSTANCE);

	@Override
	public void write(DataOutput output, Address address) throws IOException {
		this.serializer.write(output, address);
	}

	@Override
	public Address read(DataInput input) throws IOException {
		return this.serializer.read(input);
	}

	@Override
	public OptionalInt size(Address address) {
		return this.serializer.size(address);
	}

	/**
	 * Provides an externalizer of a local Infinispan address.
	 */
	@MetaInfServices(ExternalizerProvider.class)
	public static class LocalAddressExternalizerProvider implements ExternalizerProvider {
		private final Externalizer externalizer = new SerializerExternalizer(INSTANCE);

		/**
		 * Creates an externalizer provider of a local Infinispan address.
		 */
		public LocalAddressExternalizerProvider() {
		}

		@Override
		public Class<?> getType() {
			return LocalModeAddress.INSTANCE.getClass();
		}

		@Override
		public Externalizer getExternalizer() {
			return this.externalizer;
		}
	}

	/**
	 * A formatter of a local Infinispan address.
	 */
	@MetaInfServices(Formatter.class)
	public static class LocalAddressFormatter extends Formatter.Provided<Address> {
		/**
		 * Creates a formatter of a local Infinispan address.
		 */
		public LocalAddressFormatter() {
			super(Formatter.of(LocalModeAddress.INSTANCE));
		}
	}
}
