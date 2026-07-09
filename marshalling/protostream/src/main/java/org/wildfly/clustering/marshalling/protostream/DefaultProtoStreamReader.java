/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.infinispan.protostream.ProtobufTagMarshaller;
import org.infinispan.protostream.TagReader;
import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.function.BooleanSupplier;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.IntPredicate;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.function.Supplier;

/**
 * {@link ProtoStreamWriter} implementation that reads from a {@link TagReader}.
 * @author Paul Ferraro
 */
public class DefaultProtoStreamReader extends AbstractProtoStreamOperation implements ProtoStreamReader, ProtobufTagMarshaller.ReadContext, java.io.ObjectInputFilter.FilterInfo {
	private static final Function<ObjectInputFilter, Function<ObjectInputFilter.FilterInfo, ObjectInputFilter.Status>> CHECK_INPUT = new Function<>() {
		@Override
		public Function<ObjectInputFilter.FilterInfo, ObjectInputFilter.Status> apply(ObjectInputFilter filter) {
			return new Function<>() {
				@Override
				public ObjectInputFilter.Status apply(ObjectInputFilter.FilterInfo info) {
					return filter.checkInput(info);
				}
			};
		}
	};

	interface ProtoStreamReaderContext extends ProtoStreamOperation.Context {
		/**
		 * Resolves an object from the specified reference.
		 * @param reference an object reference
		 * @return the resolved object
		 */
		Object resolve(Reference reference);

		/**
		 * Returns the number of references within this context.
		 * @return the number of references within this context.
		 */
		int getReferences();
	}

	private final TagReader reader;
	private final ProtoStreamReaderContext context;
	private final BooleanSupplier depthCheck;
	private final Optional<Predicate<Class<?>>> resolvedClassCheck;
	private final Optional<IntPredicate> arrayLengthCheck;
	private final int limit;
	private int depth = 1;
	private int currentTag;

	DefaultProtoStreamReader(ProtobufTagMarshaller.ReadContext readContext, ImmutableSerializationContext context) {
		this(readContext, context, new DefaultProtoStreamReaderContext());
	}

	private DefaultProtoStreamReader(ProtobufTagMarshaller.ReadContext readContext, ImmutableSerializationContext context, ProtoStreamReaderContext readerContext) {
		super(readContext, context);
		this.reader = readContext.getReader();
		this.context = readerContext;
		Optional<Function<ObjectInputFilter.FilterInfo, ObjectInputFilter.Status>> checkInput = context.getConfiguration().getObjectInputFilter().map(CHECK_INPUT);
		Predicate<ObjectInputFilter.Status> permitted = Predicate.not(Predicate.identicalTo(ObjectInputFilter.Status.REJECTED));
		this.depthCheck = checkInput.map(function -> Supplier.of(this).thenApply(function).thenTest(permitted)).orElse(BooleanSupplier.of(true));
		this.resolvedClassCheck = checkInput.map(function -> function.compose(this::withResolvedClass).thenTest(permitted));
		this.arrayLengthCheck = checkInput.map(function -> function.composeInt(this::withArrayLength).thenTest(permitted));
		try {
			this.limit = this.reader.pushLimit(0);
			this.reader.popLimit(this.limit);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Context getContext() {
		return this.context;
	}

	@Override
	public TagReader getReader() {
		return this.reader;
	}

	@Override
	public <T> FieldSetReader<T> createFieldSetReader(FieldReadable<T> reader, int startIndex) {
		int endIndex = reader.nextIndex(startIndex);
		ProtoStreamReader offsetReader = new OffsetProtoStreamReader(this, startIndex);
		return new FieldSetReader<>() {
			@Override
			public T readField(T current) throws IOException {
				int tag = offsetReader.getCurrentTag();
				// Determine index relative to this field set
				int relativeIndex = WireType.getTagFieldNumber(tag) - startIndex;
				return reader.readFrom(offsetReader, relativeIndex, WireType.fromTag(tag), current);
			}

			@Override
			public boolean contains(int index) {
				return (index >= startIndex) && (index < endIndex);
			}
		};
	}

	private ObjectInputFilter.FilterInfo withResolvedClass(Class<?> resolvedClass) {
		return new AbstractFilterInfo(this) {
			@Override
			public Class<?> serialClass() {
				return resolvedClass;
			}
		};
	}

	private ObjectInputFilter.FilterInfo withArrayLength(int length) {
		return new AbstractFilterInfo(this) {
			@Override
			public long arrayLength() {
				return length;
			}
		};
	}

	@Override
	public Predicate<Class<?>> getResolvedClassPredicate() {
		return this.resolvedClassCheck.orElse(Predicate.of(true));
	}

	@Override
	public IntPredicate getRepeatedFieldPredicate() {
		return this.arrayLengthCheck.orElse(IntPredicate.of(true));
	}

	@Override
	public int pushLimit(int limit) throws IOException {
		this.depth += 1L;
		return this.reader.pushLimit(limit);
	}

	@Override
	public void popLimit(int oldLimit) {
		this.reader.popLimit(oldLimit);
		this.depth -= 1L;
	}

	@Override
	public int readTag() throws IOException {
		int tag = this.reader.readTag();
		this.currentTag = tag;
		return tag;
	}

	@Override
	public byte readByteTag() throws IOException {
		byte tag = this.reader.readByteTag();
		this.currentTag = tag;
		return tag;
	}

	@Override
	public int getCurrentTag() {
		return this.currentTag;
	}

	@Override
	public void checkLastTagWas(int tag) throws IOException {
		this.reader.checkLastTagWas(tag);
	}

	@Override
	public boolean skipField(int tag) throws IOException {
		this.currentTag = 0;
		return this.reader.skipField(tag);
	}

	@Override
	public boolean isAtEnd() throws IOException {
		return this.reader.isAtEnd();
	}

	@Override
	public Object readAny() throws IOException {
		Object result = this.readObject(Any.class).get();
		if (result instanceof Reference reference) {
			result = this.context.resolve(reference);
		} else {
			this.context.record(result);
		}
		return result;
	}

	@Override
	public <T> T readObject(Class<T> targetClass) throws IOException {
		this.verifyWireType(WireType.LENGTH_DELIMITED);
		int limit = this.reader.readUInt32();
		int oldLimit = this.pushLimit(limit);
		try {
			if (!this.depthCheck.getAsBoolean()) {
				throw new ArrayIndexOutOfBoundsException(this.depth);
			}
			ProtoStreamMarshaller<T> marshaller = this.findMarshaller(targetClass);
			T result = marshaller.readFrom(this);
			// Ensure marshaller reached limit
			this.reader.checkLastTagWas(0);
			return result;
		} finally {
			this.popLimit(oldLimit);
		}
	}

	@Override
	public boolean readBool() throws IOException {
		this.verifyWireType(WireType.VARINT);
		this.currentTag = 0;
		return this.reader.readBool();
	}

	@Override
	public int readEnum() throws IOException {
		this.verifyWireType(WireType.VARINT);
		this.currentTag = 0;
		return this.reader.readEnum();
	}

	@Deprecated
	@Override
	public int readInt32() throws IOException {
		this.verifyWireType(WireType.VARINT);
		this.currentTag = 0;
		return this.reader.readInt32();
	}

	@Deprecated
	@Override
	public int readFixed32() throws IOException {
		this.verifyWireType(WireType.FIXED32);
		this.currentTag = 0;
		return this.reader.readFixed32();
	}

	@Override
	public int readUInt32() throws IOException {
		// Used with unsigned byte/short/int or length records
		this.verifyWireType(EnumSet.of(WireType.VARINT, WireType.LENGTH_DELIMITED));
		this.currentTag = 0;
		return this.reader.readUInt32();
	}

	@Override
	public int readSInt32() throws IOException {
		this.verifyWireType(WireType.VARINT);
		this.currentTag = 0;
		return this.reader.readSInt32();
	}

	@Override
	public int readSFixed32() throws IOException {
		this.verifyWireType(WireType.FIXED32);
		this.currentTag = 0;
		return this.reader.readSFixed32();
	}

	@Deprecated
	@Override
	public long readInt64() throws IOException {
		this.verifyWireType(WireType.VARINT);
		this.currentTag = 0;
		return this.reader.readInt64();
	}

	@Deprecated
	@Override
	public long readFixed64() throws IOException {
		this.verifyWireType(WireType.FIXED64);
		this.currentTag = 0;
		return this.reader.readFixed64();
	}

	@Override
	public long readUInt64() throws IOException {
		this.verifyWireType(WireType.VARINT);
		this.currentTag = 0;
		return this.reader.readUInt64();
	}

	@Override
	public long readSInt64() throws IOException {
		this.verifyWireType(WireType.VARINT);
		this.currentTag = 0;
		return this.reader.readSInt64();
	}

	@Override
	public long readSFixed64() throws IOException {
		this.verifyWireType(WireType.FIXED64);
		this.currentTag = 0;
		return this.reader.readSFixed64();
	}

	@Override
	public float readFloat() throws IOException {
		// Used with float and packed float arrays
		this.verifyWireType(EnumSet.of(WireType.FIXED32, WireType.VARINT));
		this.currentTag = 0;
		return this.reader.readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		// Used with double and packed double arrays
		this.verifyWireType(EnumSet.of(WireType.FIXED64, WireType.VARINT));
		this.currentTag = 0;
		return this.reader.readDouble();
	}

	@Override
	public byte[] readByteArray() throws IOException {
		this.verifyWireType(WireType.LENGTH_DELIMITED);
		this.currentTag = 0;
		return this.reader.readByteArray();
	}

	@Override
	public ByteBuffer readByteBuffer() throws IOException {
		this.verifyWireType(WireType.LENGTH_DELIMITED);
		this.currentTag = 0;
		return this.reader.readByteBuffer();
	}

	@Override
	public String readString() throws IOException {
		this.verifyWireType(WireType.LENGTH_DELIMITED);
		this.currentTag = 0;
		return this.reader.readString();
	}

	@Override
	public byte[] fullBufferArray() throws IOException {
		this.verifyWireType(WireType.LENGTH_DELIMITED);
		return this.reader.fullBufferArray();
	}

	@Override
	public InputStream fullBufferInputStream() throws IOException {
		this.verifyWireType(WireType.LENGTH_DELIMITED);
		return this.reader.fullBufferInputStream();
	}

	@Override
	public boolean isInputStream() {
		return this.reader.isInputStream();
	}

	@Override
	public Class<?> serialClass() {
		return null;
	}

	@Override
	public long arrayLength() {
		return -1L;
	}

	@Override
	public long depth() {
		return this.depth;
	}

	@Override
	public long references() {
		return this.context.getReferences();
	}

	@Override
	public long streamBytes() {
		return this.limit;
	}

	private void verifyWireType(WireType type) throws IOException {
		this.verifyWireType(EnumSet.of(type));
	}

	private void verifyWireType(Set<WireType> types) throws IOException {
		WireType currentType = WireType.fromTag(this.currentTag);
		if (!types.contains(currentType)) {
			throw new IllegalStateException(currentType.name());
		}
	}

	private static class DefaultProtoStreamReaderContext implements ProtoStreamReaderContext {
		private static final int REFERENCE_INITIAL_CAPACITY = 128;
		private final Map<Object, Boolean> objects = new IdentityHashMap<>(REFERENCE_INITIAL_CAPACITY);
		private final List<Object> references = new ArrayList<>(REFERENCE_INITIAL_CAPACITY);

		@Override
		public void record(Object object) {
			if (object != null) {
				if (this.objects.putIfAbsent(object, Boolean.TRUE) == null) {
					this.references.add(object);
				}
			}
		}

		@Override
		public Object resolve(Reference reference) {
			return this.references.get(reference.getAsInt());
		}

		@Override
		public int getReferences() {
			return this.references.size();
		}
	}

	private abstract static class AbstractFilterInfo implements ObjectInputFilter.FilterInfo {
		private final ObjectInputFilter.FilterInfo info;

		AbstractFilterInfo(ObjectInputFilter.FilterInfo info) {
			this.info = info;
		}

		@Override
		public Class<?> serialClass() {
			return this.info.serialClass();
		}

		@Override
		public long arrayLength() {
			return this.info.arrayLength();
		}

		@Override
		public long depth() {
			return this.info.depth();
		}

		@Override
		public long references() {
			return this.info.references();
		}

		@Override
		public long streamBytes() {
			return this.info.streamBytes();
		}
	}
}
