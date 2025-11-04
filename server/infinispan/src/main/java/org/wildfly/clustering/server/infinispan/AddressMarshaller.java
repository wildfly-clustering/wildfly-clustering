/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.IOException;
import java.util.UUID;

import org.infinispan.protostream.descriptors.WireType;
import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.NodeVersion;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * ProtoStream marshaller for an {@link Address}.
 * @author Paul Ferraro
 */
public enum AddressMarshaller implements ProtoStreamMarshaller<Address> {
	/** Singleton instance */
	INSTANCE;

	private static final int UUID_INDEX = 1;
	private static final int VERSION_INDEX = 2;
	private static final int SITE_INDEX = 3;
	private static final int RACK_INDEX = 4;
	private static final int MACHINE_INDEX = 5;

	@Override
	public Class<? extends Address> getJavaClass() {
		return Address.class;
	}

	@Override
	public Address readFrom(ProtoStreamReader reader) throws IOException {
		UUID id = null;
		NodeVersion version = null;
		String site = null;
		String rack = null;
		String machine = null;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case UUID_INDEX -> id = reader.readObject(UUID.class);
				case VERSION_INDEX -> version = reader.readObject(NodeVersion.class);
				case SITE_INDEX -> site = reader.readString();
				case RACK_INDEX -> rack = reader.readString();
				case MACHINE_INDEX -> machine = reader.readString();
				default -> reader.skipField(tag);
			}
		}
		return (id != null) ? Address.protoFactory(id.getMostSignificantBits(), id.getLeastSignificantBits(), version, site, rack, machine) : Address.LOCAL;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Address address) throws IOException {
		if (address != Address.LOCAL) {
			writer.writeObject(UUID_INDEX, address.getNodeUUID());
			NodeVersion version = address.getVersion();
			if (version != null) {
				writer.writeObject(VERSION_INDEX, version);
			}
			String site = address.getSiteId();
			if (site != null) {
				writer.writeString(SITE_INDEX, site);
			}
			String rack = address.getRackId();
			if (rack != null) {
				writer.writeString(RACK_INDEX, rack);
			}
			String machine = address.getMachineId();
			if (machine != null) {
				writer.writeString(MACHINE_INDEX, machine);
			}
		}
	}
}
