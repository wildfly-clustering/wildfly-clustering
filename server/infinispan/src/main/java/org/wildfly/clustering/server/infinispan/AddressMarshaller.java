/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.IOException;

import org.infinispan.protostream.descriptors.WireType;
import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.marshalling.protostream.FieldSetMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshallers for the fields of an {@link Address}.
 * @author Paul Ferraro
 */
public enum AddressMarshaller implements FieldSetMarshaller.Simple<Address> {
	INSTANCE;

	private static final int JGROUPS_ADDRESS_INDEX = 0;
	private static final int FIELDS = 1;

	@Override
	public Address createInitialValue() {
		return LocalModeAddress.INSTANCE;
	}

	@Override
	public int getFields() {
		return FIELDS;
	}

	@Override
	public Address readFrom(ProtoStreamReader reader, int index, WireType type, Address address) throws IOException {
		return switch (index) {
			case JGROUPS_ADDRESS_INDEX -> reader.readObject(JGroupsAddress.class);
			default -> Supplier.call(() -> reader.skipField(type), null).thenApply(Function.of(address)).get();
		};
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Address address) throws IOException {
		if (address instanceof JGroupsAddress) {
			writer.writeObject(JGROUPS_ADDRESS_INDEX, address);
		}
	}
}
