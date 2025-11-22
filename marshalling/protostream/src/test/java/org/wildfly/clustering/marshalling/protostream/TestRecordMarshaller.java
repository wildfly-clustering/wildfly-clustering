/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.test.TestRecord;

/**
 * @author Paul Ferraro
 */
public enum TestRecordMarshaller implements ProtoStreamMarshaller<TestRecord> {
	INSTANCE;

	private static final int NAME_INDEX = 1;
	private static final int VALUE_INDEX = 2;

	@Override
	public Class<? extends TestRecord> getJavaClass() {
		return TestRecord.class;
	}

	@Override
	public TestRecord readFrom(ProtoStreamReader reader) throws IOException {
		String name = null;
		Integer value = null;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case NAME_INDEX -> name = reader.readString();
				case VALUE_INDEX -> value = reader.readSFixed32();
				default -> reader.skipField(tag);
			}
		}
		return new TestRecord(name, value);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, TestRecord record) throws IOException {
		if (record.name() != null) {
			writer.writeString(NAME_INDEX, record.name());
		}
		if (record.value() != null) {
			writer.writeSFixed32(VALUE_INDEX, record.value());
		}
	}
}
