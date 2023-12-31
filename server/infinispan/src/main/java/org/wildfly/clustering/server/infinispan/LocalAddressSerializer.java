/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.BinaryFormatter;
import org.wildfly.clustering.marshalling.Formatter;
import org.wildfly.clustering.marshalling.Serializer;

/**
 * Serializer for a local {@link Address}.
 * @author Paul Ferraro
 */
public enum LocalAddressSerializer implements Serializer<Address> {
	INSTANCE;

	@Override
	public void write(DataOutput output, Address value) throws IOException {
	}

	@Override
	public Address read(DataInput input) throws IOException {
		return LocalModeAddress.INSTANCE;
	}

	@MetaInfServices(Formatter.class)
	public static class LocalAddressFormatter extends BinaryFormatter<Address> {
		@SuppressWarnings("unchecked")
		public LocalAddressFormatter() {
			super((Class<Address>) (Class<?>) LocalModeAddress.class, INSTANCE);
		}
	}
}
