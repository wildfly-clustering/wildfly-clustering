/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.infinispan.protostream.descriptors.WireType;

/**
 * A set of fields used by {@link AnyMarshaller}.
 * @author Paul Ferraro
 */
enum AnyField implements Field<Object> {
	ANY(Scalar.ANY), // For re-use by other fields
	REFERENCE(Scalar.REFERENCE),
	BOOLEAN(Scalar.BOOLEAN),
	BYTE(Scalar.BYTE),
	SHORT(Scalar.SHORT),
	INTEGER(Scalar.INTEGER),
	LONG(Scalar.LONG),
	FLOAT(Scalar.FLOAT),
	DOUBLE(Scalar.DOUBLE),
	CHARACTER(Scalar.CHARACTER),
	STRING(Scalar.STRING),
	IDENTIFIED_OBJECT(new TypedObjectMarshaller(ScalarClass.ID)),
	IDENTIFIED_ENUM(new TypedEnumMarshaller<>(ScalarClass.ID)),
	IDENTIFIED_ARRAY(new TypedArrayMarshaller(ScalarClass.ID)),
	FIELD_ARRAY(new TypedArrayMarshaller(ScalarClass.FIELD)),
	// Prioritize the fields above:  https://developers.google.com/protocol-buffers/docs/proto#assigning_field_numbers
	NAMED_OBJECT(new TypedObjectMarshaller(ScalarClass.NAME)),
	NAMED_ENUM(new TypedEnumMarshaller<>(ScalarClass.NAME)),
	NAMED_ARRAY(new TypedArrayMarshaller(ScalarClass.NAME)),
	BOOLEAN_ARRAY(new FieldMarshaller<boolean[]>() {
		// Optimize using a BitSet, rather than a packed array of booleans
		@Override
		public boolean[] readFrom(ProtoStreamReader reader) throws IOException {
			byte[] bytes = Scalar.BYTE_ARRAY.cast(byte[].class).readFrom(reader);
			int length = bytes.length;
			while (!reader.isAtEnd()) {
				int tag = reader.readTag();
				int index = WireType.getTagFieldNumber(tag);
				if (index == INTEGER.getIndex()) {
					// Adjust length of boolean[]
					length = ((bytes.length - 1) * Byte.SIZE) + reader.readUInt32();
				} else {
					reader.skipField(tag);
				}
			}
			BitSet set = BitSet.valueOf(bytes);
			boolean[] values = new boolean[length];
			for (int i = 0; i < length; ++i) {
				values[i] = set.get(i);
			}
			return values;
		}

		@Override
		public void writeTo(ProtoStreamWriter writer, boolean[] values) throws IOException {
			int length = values.length;
			// Pack boolean values into BitSet
			BitSet set = new BitSet(length);
			for (int i = 0; i < length; ++i) {
				set.set(i, values[i]);
			}
			byte[] bytes = set.toByteArray();
			Scalar.BYTE_ARRAY.cast(byte[].class).writeTo(writer, bytes);
			// boolean[] length might be shorter than the BitSet, if so, write the difference
			int remainder = length % Byte.SIZE;
			if (remainder > 0) {
				writer.writeUInt32(INTEGER.getIndex(), remainder);
			}
		}

		@Override
		public Class<? extends boolean[]> getJavaClass() {
			return boolean[].class;
		}

		@Override
		public WireType getWireType() {
			return Scalar.BYTE_ARRAY.getWireType();
		}
	}),
	BYTE_ARRAY(Scalar.BYTE_ARRAY),
	SHORT_ARRAY(new PackedArrayMarshaller<>(Short.TYPE, Scalar.SHORT.cast(Short.class))),
	INTEGER_ARRAY(new PackedArrayMarshaller<>(Integer.TYPE, Scalar.INTEGER.cast(Integer.class))),
	LONG_ARRAY(new PackedArrayMarshaller<>(Long.TYPE, Scalar.LONG.cast(Long.class))),
	FLOAT_ARRAY(new PackedArrayMarshaller<>(Float.TYPE, Scalar.FLOAT.cast(Float.class))),
	DOUBLE_ARRAY(new PackedArrayMarshaller<>(Double.TYPE, Scalar.DOUBLE.cast(Double.class))),
	CHAR_ARRAY(new PackedArrayMarshaller<>(Character.TYPE, Scalar.CHARACTER.cast(Character.class))),
	ANY_ARRAY(new TypedArrayMarshaller(ScalarClass.ANY)),
	PROXY(new FieldMarshaller<>() {
		@Override
		public Object readFrom(ProtoStreamReader reader) throws IOException {
			InvocationHandler handler = (InvocationHandler) Scalar.ANY.readFrom(reader);
			List<Class<?>> interfaces = new LinkedList<>();
			while (!reader.isAtEnd()) {
				int tag = reader.readTag();
				int index = WireType.getTagFieldNumber(tag);
				if (index == ANY.getIndex()) {
					interfaces.add(ScalarClass.ANY.readFrom(reader));
				} else {
					reader.skipField(tag);
				}
			}
			return Proxy.newProxyInstance(Privileged.getClassLoader(handler.getClass()), interfaces.toArray(new Class<?>[0]), handler);
		}

		@Override
		public void writeTo(ProtoStreamWriter writer, Object proxy) throws IOException {
			Scalar.ANY.writeTo(writer, Proxy.getInvocationHandler(proxy));

			for (Class<?> interfaceClass : proxy.getClass().getInterfaces()) {
				writer.writeTag(ANY.getIndex(), ScalarClass.ANY.getWireType());
				ScalarClass.ANY.writeTo(writer, interfaceClass);
			}
		}

		@Override
		public Class<? extends Object> getJavaClass() {
			return Object.class;
		}

		@Override
		public WireType getWireType() {
			return WireType.LENGTH_DELIMITED;
		}
	}),
	;
	private final FieldMarshaller<Object> marshaller;

	AnyField(ScalarMarshaller<?> marshaller) {
		this(new ScalarFieldMarshaller<>(marshaller));
	}

	@SuppressWarnings("unchecked")
	AnyField(FieldMarshaller<?> marshaller) {
		this.marshaller = (FieldMarshaller<Object>) marshaller;
	}

	@Override
	public int getIndex() {
		return this.ordinal() + 1;
	}

	@Override
	public FieldMarshaller<Object> getMarshaller() {
		return this.marshaller;
	}

	private static final AnyField[] VALUES = AnyField.values();

	static AnyField fromIndex(int index) {
		return (index > 0) && (index <= VALUES.length) ? VALUES[index - 1] : null;
	}

	private static final Map<Class<?>, AnyField> FIELDS = new IdentityHashMap<>();
	static {
		for (AnyField field : VALUES) {
			Class<?> fieldClass = field.getMarshaller().getJavaClass();
			if ((fieldClass != Object.class) && (fieldClass != Enum.class) && fieldClass != Class.class) {
				FIELDS.put(fieldClass, field);
			}
		}
	}

	static AnyField fromJavaType(Class<?> targetClass) {
		return FIELDS.get(targetClass);
	}
}
