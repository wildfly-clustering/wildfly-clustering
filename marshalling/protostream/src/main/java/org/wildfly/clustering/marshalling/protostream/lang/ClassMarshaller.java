/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.lang;

import java.io.IOException;

import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.ImmutableSerializationContext;
import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ClassLoaderResolver;
import org.wildfly.clustering.marshalling.protostream.Field;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Generic marshaller for instances of {@link Class}.
 * @author Paul Ferraro
 */
class ClassMarshaller implements ProtoStreamMarshaller<Class<?>> {

	private final Field<Class<?>> field;

	ClassMarshaller(ClassLoaderResolver resolver) {
		ClassField[] fields = ClassField.values();
		this.field = new ResolvedClassField(resolver, fields[fields.length - 1].getIndex() + 1);
	}

	@Override
	public Class<?> readFrom(ProtoStreamReader reader) throws IOException {
		Class<?> result = Object.class;
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			int index = WireType.getTagFieldNumber(tag);
			Field<Class<?>> field = index == this.field.getIndex() ? this.field : ClassField.fromIndex(index);
			if (field != null) {
				result = field.getMarshaller().readFrom(reader);
			} else {
				reader.skipField(tag);
			}
		}
		if (!reader.getResolvedClassPredicate().test(result)) {
			throw new IllegalArgumentException(result.getCanonicalName());
		}
		return result;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Class<?> targetClass) throws IOException {
		if (targetClass != Object.class) {
			Field<Class<?>> field = this.getField(writer.getSerializationContext(), targetClass);
			writer.writeTag(field.getIndex(), field.getMarshaller().getWireType());
			field.getMarshaller().writeTo(writer, targetClass);
		}
	}

	Field<Class<?>> getField(ImmutableSerializationContext context, Class<?> targetClass) {
		Field<?> classField = Field.forClass(targetClass);
		if (classField != null) return ClassField.FIELD;
		if (targetClass.isArray()) return ClassField.ARRAY;
		try {
			BaseMarshaller<?> marshaller = context.getMarshaller(targetClass);
			if (marshaller.getJavaClass() != targetClass) return this.field;
			return context.getDescriptorByName(marshaller.getTypeName()).getTypeId() != null ? ClassField.ID : ClassField.NAME;
		} catch (IllegalArgumentException e) {
			// If class does not represent a registered type, then use the loader based marshaller.
			return this.field;
		}
	}

	@Override
	public Class<? extends Class<?>> getJavaClass() {
		return this.field.getMarshaller().getJavaClass();
	}
}
