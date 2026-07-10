/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infinispan.protostream.TagReader;
import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.function.IntPredicate;
import org.wildfly.clustering.function.Predicate;

/**
 * A {@link TagReader} with the additional ability to read an arbitrary embedded object.
 * @author Paul Ferraro
 */
public interface ProtoStreamReader extends ProtoStreamOperation, TagReader {

	/**
	 * Returns a filter for validating a resolved class.
	 * @return a filter for validating a resolved class.
	 */
	Predicate<Class<?>> getResolvedClassPredicate();

	/**
	 * Returns the filter for validating the length of a repeated field.
	 * @return the filter for validating the length of a repeated field.
	 */
	IntPredicate getRepeatedFieldPredicate();

	/**
	 * Returns a list for collecting values for repeated fields.
	 * The returned list will not grow larger than the configured limits.
	 * @param <T> the field type
	 * @return a list for collecting values for repeated fields.
	 */
	default <T> List<T> repeatedElementCollector() {
		return new ArrayList<>(DefaultProtoStreamConfiguration.REPEATED_FIELD_CAPACITY) {
			@Override
			public boolean add(T value) {
				// Validate growth in chunks
				if (!this.isEmpty() && ((this.size() % DefaultProtoStreamConfiguration.REPEATED_FIELD_CHUNK_SIZE) == 0) && !ProtoStreamReader.this.getRepeatedFieldPredicate().test(this.size())) {
					throw new ArrayIndexOutOfBoundsException(this.size());
				}
				return super.add(value);
			}
		};
	}

	/**
	 * Returns a map for collecting entries for repeated fields.
	 * The returned map will not grow larger than the configured limits.
	 * @param <K> the entry key type
	 * @param <V> the entry value type
	 * @return a map for collecting entries for repeated fields.
	 */
	default <K, V> Map<K, V> repeatedEntryCollector() {
		// Limit the number of length validations by chunks
		return new HashMap<>(DefaultProtoStreamConfiguration.REPEATED_FIELD_CAPACITY) {
			@Override
			public V put(K key, V value) {
				// Validate growth in chunks
				if (!this.isEmpty() && ((this.size() % DefaultProtoStreamConfiguration.REPEATED_FIELD_CHUNK_SIZE) == 0) && !ProtoStreamReader.this.getRepeatedFieldPredicate().test(this.size())) {
					throw new ArrayIndexOutOfBoundsException(this.size());
				}
				return super.put(key, value);
			}
		};
	}

	/**
	 * Returns the tag of the current field, or 0 if {@link #readTag()} was not yet called for the next field.
	 * @return the tag of the current field.
	 */
	int getCurrentTag();

	/**
	 * Skips over the field of the specified wire type.
	 * @param type the expected wire type of the field to skip.
	 * @return true, if the current tag is a normal field, false otherwise
	 * @throws IOException if the stream does not conform to the wire type of the skipped field.
	 */
	default boolean skipField(WireType type) throws IOException {
		return this.skipField(WireType.makeTag(0, type));
	}

	/**
	 * Skips over the field of the specified wire type, returning the specified object.
	 * @param <T> the return type
	 * @param type the expected wire type of the field to skip.
	 * @param value the value assumed by this skipped field
	 * @return the specified object
	 * @throws IOException if the stream does not conform to the wire type of the skipped field.
	 */
	default <T> T skipField(WireType type, T value) throws IOException {
		this.skipField(type);
		return value;
	}

	/**
	 * Returns a reader for a field set whose fields start at the specified index.
	 * @param <T> the field builder type
	 * @param reader a field reader
	 * @param startIndex the start index for the field set
	 * @return a field set reader
	 */
	<T> FieldSetReader<T> createFieldSetReader(FieldReadable<T> reader, int startIndex);

	/**
	 * Reads an object of an arbitrary type from this reader.
	 * @return a supplier of the unmarshalled object
	 * @throws IOException if the object could not be read with the associated marshaller.
	 */
	Object readAny() throws IOException;

	/**
	 * Reads an object of an arbitrary type from this reader, cast to the specified type.
	 * @param <T> the target type
	 * @param targetClass the target type
	 * @return a supplier of the unmarshalled object
	 * @throws IOException if the object could not be read with the associated marshaller.
	 */
	default <T> T readAny(Class<T> targetClass) throws IOException {
		return targetClass.cast(this.readAny());
	}

	/**
	 * Reads an object of the specified type from this reader.
	 * @param <T> the type of the associated marshaller
	 * @param targetClass the class of the associated marshaller
	 * @return the unmarshalled object
	 * @throws IOException if no marshaller is associated with the specified class, or if the object could not be read with the associated marshaller.
	 */
	<T> T readObject(Class<T> targetClass) throws IOException;

	/**
	 * Reads an enum of the specified type from this reader.
	 * @param <E> the enum type of the associated marshaller
	 * @param enumClass the class of the associated marshaller
	 * @return the unmarshalled enum
	 * @throws IOException if no marshaller is associated with the specified enum class, or if the enum could not be read with the associated marshaller.
	 */
	default <E extends Enum<E>> E readEnum(Class<E> enumClass) throws IOException {
		EnumMarshaller<E> marshaller = (EnumMarshaller<E>) this.getSerializationContext().getMarshaller(enumClass);
		int code = this.readEnum();
		return marshaller.decode(code);
	}

	/**
	 * Deprecated to discourage use.
	 * @deprecated Use {@link #readUInt32()} or {@link #readSInt32()}
	 */
	@Deprecated
	@Override
	int readInt32() throws IOException;

	/**
	 * Deprecated to discourage use.
	 * @deprecated Use {@link #readSFixed32()} instead.
	 */
	@Deprecated
	@Override
	int readFixed32() throws IOException;

	/**
	 * Deprecated to discourage use.
	 * @deprecated Use {@link #readUInt64()} or {@link #readSInt64()}
	 */
	@Deprecated
	@Override
	long readInt64() throws IOException;

	/**
	 * Deprecated to discourage use.
	 * @deprecated Use {@link #readSFixed64()} instead.
	 */
	@Deprecated
	@Override
	long readFixed64() throws IOException;
}
