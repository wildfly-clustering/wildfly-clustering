/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.stream.IntStream;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * ProtoStream marshaller for an {@link EnumSet}.
 * @author Paul Ferraro
 * @param <E> enum type
 */
class EnumSetMarshaller<E extends Enum<E>> implements ProtoStreamMarshaller<EnumSet<E>> {
	static final ProtoStreamMarshaller<?> INSTANCE = new EnumSetMarshaller<>();

	static final Field ENUM_SET_CLASS_FIELD = Reflect.findField(EnumSet.class, Class.class);

	private static final int CLASS_INDEX = 1;
	private static final int COMPLEMENT_CLASS_INDEX = 2;
	private static final int BITS_INDEX = 3;
	private static final int ELEMENT_INDEX = 4;

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends EnumSet<E>> getJavaClass() {
		return (Class<? extends EnumSet<E>>) (Class<?>) EnumSet.class;
	}

	@Override
	public EnumSet<E> readFrom(ProtoStreamReader reader) throws IOException {
		IntStream.Builder elements = IntStream.builder();
		Class<E> enumClass = null;
		boolean complement = false;
		BitSet bits = null;

		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case COMPLEMENT_CLASS_INDEX:
					complement = true;
					// Fall through
				case CLASS_INDEX:
					enumClass = reader.readObject(Class.class);
					break;
				case BITS_INDEX:
					bits = reader.readObject(BitSet.class);
					break;
				case ELEMENT_INDEX:
					elements.accept(reader.readUInt32());
					break;
				default:
					reader.skipField(tag);
			}
		}

		EnumSet<E> set = EnumSet.noneOf(enumClass);
		E[] values = enumClass.getEnumConstants();
		if (bits != null) {
			for (int i = 0; i < values.length; ++i) {
				if (bits.get(i)) {
					set.add(values[i]);
				}
			}
		} else {
			elements.build().mapToObj(index -> values[index]).forEach(set::add);
		}
		return complement ? EnumSet.complementOf(set) : set;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, EnumSet<E> set) throws IOException {
		Class<E> enumClass = this.findEnumClass(set);
		E[] values = enumClass.getEnumConstants();
		// Marshal the smaller of the set versus the set's complement
		boolean complement = set.size() * 2 > values.length;

		writer.writeObject(complement ? COMPLEMENT_CLASS_INDEX : CLASS_INDEX, enumClass);

		EnumSet<E> targetSet = complement ? EnumSet.complementOf(set) : set;

		// Write as BitSet or individual elements depending on size
		if (((values.length + Byte.SIZE - 1) / Byte.SIZE) < targetSet.size()) {
			BitSet bits = new BitSet(values.length);
			for (int i = 0; i < values.length; ++i) {
				bits.set(i, targetSet.contains(values[i]));
			}
			writer.writeObject(BITS_INDEX, bits);
		} else {
			for (E value : targetSet) {
				writer.writeUInt32(ELEMENT_INDEX, value.ordinal());
			}
		}
	}

	private Class<E> findEnumClass(EnumSet<E> set) {
		EnumSet<E> nonEmptySet = set.isEmpty() ? EnumSet.complementOf(set) : set;
		Iterator<E> values = nonEmptySet.iterator();
		if (!values.hasNext()) {
			throw new IllegalStateException();
		}
		return values.next().getDeclaringClass();
	}
}
