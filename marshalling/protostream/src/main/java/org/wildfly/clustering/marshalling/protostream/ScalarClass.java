/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.ImmutableSerializationContext;
import org.infinispan.protostream.descriptors.WireType;

/**
 * Set of scalar marshallers for marshalling a {@link Class}.
 * @author Paul Ferraro
 */
public enum ScalarClass implements ScalarMarshaller<Class<?>> {

	ANY(WireType.LENGTH_DELIMITED) {
		@Override
		public Class<?> readFrom(ProtoStreamReader reader) throws IOException {
			return reader.readObject(Class.class);
		}

		@Override
		public void writeTo(ProtoStreamWriter writer, Class<?> value) throws IOException {
			writer.writeObjectNoTag(value);
		}
	},
	ID(WireType.VARINT) {
		@Override
		public Class<?> readFrom(ProtoStreamReader reader) throws IOException {
			int typeId = reader.readUInt32();
			ImmutableSerializationContext context = reader.getSerializationContext();
			String typeName = context.getDescriptorByTypeId(typeId).getFullName();
			BaseMarshaller<?> marshaller = context.getMarshaller(typeName);
			return marshaller.getJavaClass();
		}

		@Override
		public void writeTo(ProtoStreamWriter writer, Class<?> value) throws IOException {
			BaseMarshaller<?> marshaller = writer.findMarshaller(value);
			String typeName = marshaller.getTypeName();
			int typeId = writer.getSerializationContext().getDescriptorByName(typeName).getTypeId();
			writer.writeVarint32(typeId);
		}
	},
	NAME(WireType.LENGTH_DELIMITED) {
		@Override
		public Class<?> readFrom(ProtoStreamReader reader) throws IOException {
			String typeName = StandardCharsets.UTF_8.decode(reader.readByteBuffer()).toString();
			BaseMarshaller<?> marshaller = reader.getSerializationContext().getMarshaller(typeName);
			return marshaller.getJavaClass();
		}

		@Override
		public void writeTo(ProtoStreamWriter writer, Class<?> value) throws IOException {
			BaseMarshaller<?> marshaller = writer.findMarshaller(value);
			String typeName = marshaller.getTypeName();
			Scalar.BYTE_BUFFER.writeTo(writer, StandardCharsets.UTF_8.encode(typeName));
		}
	},
	FIELD(WireType.VARINT) {
		@Override
		public Class<?> readFrom(ProtoStreamReader reader) throws IOException {
			return AnyField.fromIndex(reader.readUInt32() + 1).getMarshaller().getJavaClass();
		}

		@Override
		public void writeTo(ProtoStreamWriter writer, Class<?> value) throws IOException {
			writer.writeVarint32(AnyField.fromJavaType(value).getIndex() - 1);
		}
	},
	;
	private final WireType wireType;

	ScalarClass(WireType wireType) {
		this.wireType = wireType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Class<?>> getJavaClass() {
		return (Class<? extends Class<?>>) (Class<?>) Class.class;
	}

	@Override
	public WireType getWireType() {
		return this.wireType;
	}
}
