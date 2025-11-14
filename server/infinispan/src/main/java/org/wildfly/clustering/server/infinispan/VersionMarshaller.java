/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.IOException;

import org.infinispan.protostream.descriptors.WireType;
import org.infinispan.remoting.transport.NodeVersion;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * ProtoStream marshaller for a {@link NodeVersion}.
 * @author Paul Ferraro
 */
public enum VersionMarshaller implements ProtoStreamMarshaller<NodeVersion> {
	/** Singleton instance */
	INSTANCE;

	private static final int MAJOR_INDEX = 1;
	private static final int MINOR_INDEX = 2;
	private static final int MICRO_INDEX = 3;
	private static final byte DEFAULT = 0x0;

	@Override
	public Class<? extends NodeVersion> getJavaClass() {
		return NodeVersion.class;
	}

	@Override
	public NodeVersion readFrom(ProtoStreamReader reader) throws IOException {
		byte major = DEFAULT;
		byte minor = DEFAULT;
		byte micro = DEFAULT;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case MAJOR_INDEX -> major = (byte) reader.readUInt32();
				case MINOR_INDEX -> minor = (byte) reader.readUInt32();
				case MICRO_INDEX -> micro = (byte) reader.readUInt32();
				default -> reader.skipField(tag);
			}
		}
		return NodeVersion.from(major, minor, micro);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, NodeVersion version) throws IOException {
		byte major = version.getMajor();
		if (major != DEFAULT) {
			writer.writeUInt32(MAJOR_INDEX, major);
		}
		byte minor = version.getMinor();
		if (minor != DEFAULT) {
			writer.writeUInt32(MINOR_INDEX, minor);
		}
		byte micro = version.getPatch();
		if (micro != DEFAULT) {
			writer.writeUInt32(MICRO_INDEX, micro);
		}
	}
}
