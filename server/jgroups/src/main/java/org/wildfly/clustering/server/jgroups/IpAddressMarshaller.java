/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

import org.infinispan.protostream.descriptors.WireType;
import org.jgroups.stack.IpAddress;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Marshaller for fields of a {@link IpAddress}.
 * @author Paul Ferraro
 */
public class IpAddressMarshaller implements ProtoStreamMarshaller<IpAddress> {
	static final InetAddress DEFAULT_ADDRESS = InetAddress.getLoopbackAddress();
	static final int DEFAULT_PORT = 7600; // Default TCP port

	private static final int ADDRESS_INDEX = 1;
	private static final int PORT_INDEX = 2;

	@Override
	public Class<? extends IpAddress> getJavaClass() {
		return IpAddress.class;
	}

	@Override
	public IpAddress readFrom(ProtoStreamReader reader) throws IOException {
		InetAddress address = DEFAULT_ADDRESS;
		int port = DEFAULT_PORT;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case ADDRESS_INDEX:
					address = InetAddress.getByAddress(reader.readByteArray());
					break;
				case PORT_INDEX:
					port = reader.readUInt32();
					break;
				default:
					reader.skipField(tag);
			}
		}
		return new IpAddress(address, port);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, IpAddress address) throws IOException {
		byte[] bytes = address.getIpAddress().getAddress();
		if (!Arrays.equals(bytes, DEFAULT_ADDRESS.getAddress())) {
			writer.writeBytes(ADDRESS_INDEX, bytes);
		}
		int port = address.getPort();
		if (port != DEFAULT_PORT) {
			writer.writeUInt32(PORT_INDEX, port);
		}
	}
}
