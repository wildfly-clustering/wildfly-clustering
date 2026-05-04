/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.transaction;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.infinispan.commons.tx.XidImpl;
import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * ProtoStream marshaller for a {@link XidImpl}.
 * @author Paul Ferraro
 */
public enum XidImplMarshaller implements ProtoStreamMarshaller<XidImpl> {
	/** Singleton instance */
	INSTANCE;

	private static final int FORMAT_INDEX = 1;
	private static final int GLOBAL_INDEX = 2;
	private static final int BRANCH_INDEX = 3;

	// Format identifier for RemoteXid
	private static final int DEFAULT_FORMAT_ID = 0x48525458;

	@Override
	public Class<? extends XidImpl> getJavaClass() {
		return XidImpl.class;
	}

	@Override
	public XidImpl readFrom(ProtoStreamReader reader) throws IOException {
		int formatId = DEFAULT_FORMAT_ID;
		byte[] globalId = new byte[0];
		byte[] branchId = null;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case FORMAT_INDEX -> formatId = reader.readSFixed32();
				case GLOBAL_INDEX -> globalId = reader.readByteArray();
				case BRANCH_INDEX -> branchId = reader.readByteArray();
				default -> reader.skipField(tag);
			}
		}
		return XidImpl.create(formatId, globalId, Optional.ofNullable(branchId).orElse(globalId));
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, XidImpl id) throws IOException {
		int formatId = id.getFormatId();
		if (formatId != DEFAULT_FORMAT_ID) {
			writer.writeSFixed32(FORMAT_INDEX, formatId);
		}
		byte[] globalId = id.getGlobalTransactionId();
		if (globalId.length > 0) {
			writer.writeBytes(GLOBAL_INDEX, globalId);
		}
		byte[] branchId = id.getBranchQualifier();
		if (!Arrays.equals(globalId, branchId)) {
			writer.writeBytes(BRANCH_INDEX, branchId);
		}
	}
}
