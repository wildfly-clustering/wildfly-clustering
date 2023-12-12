/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jgroups.Address;
import org.jgroups.util.Util;
import org.wildfly.clustering.marshalling.Serializer;

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
}
