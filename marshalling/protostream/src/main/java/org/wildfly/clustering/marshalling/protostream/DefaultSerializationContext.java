/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.stream.Stream;

import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.BaseMarshallerDelegate;
import org.infinispan.protostream.ProtobufTagMarshaller;
import org.infinispan.protostream.WrappedMessage;
import org.infinispan.protostream.descriptors.GenericDescriptor;
import org.infinispan.protostream.impl.TagReaderImpl;
import org.infinispan.protostream.impl.TagWriterImpl;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;

/**
 * Decorates an {@link org.infinispan.protostream.SerializationContext}, ensuring that all registered marshallers implement {@link ProtoStreamMarshaller}.
 * We have to use the decorator pattern since SerializationContextImpl is final.
 * @author Paul Ferraro
 */
class DefaultSerializationContext extends NativeSerializationContext implements SerializationContext {
	private final ProtoStreamConfiguration configuration;
	private final org.infinispan.protostream.SerializationContext context;
	private final Supplier<TagWriterImpl> sizeWriterFactory;
	private final Function<GenericDescriptor, Class<?>> type;

	/**
	 * Creates a new serialization context from the specified context
	 * @param configuration the ProtoStream configuration
	 * @param context a decorated serialization context implementation
	 */
	DefaultSerializationContext(ProtoStreamConfiguration configuration, org.infinispan.protostream.SerializationContext context) {
		super(context);
		// Unregister WrappedMessage marshaller and schema
		// We will replace this with Any
		context.unregisterMarshaller(context.getMarshaller(WrappedMessage.class));
		context.unregisterProtoFile(WrappedMessage.PROTO_FILE);
		if (context.canMarshall(WrappedMessage.class)) {
			throw new IllegalStateException();
		}
		this.configuration = configuration;
		this.context = context;
		this.sizeWriterFactory = Supplier.of(context).thenApply(TagWriterImpl::newInstance);
		Function<GenericDescriptor, Integer> typeId = GenericDescriptor::getTypeId;
		Function<GenericDescriptor, BaseMarshaller<?>> identifiedMarshaller = typeId.thenApplyAsInt(Integer::intValue).thenApply(this::getMarshallerDelegate).thenApply(BaseMarshallerDelegate::getMarshaller);
		Function<GenericDescriptor, BaseMarshaller<?>> namedMarshaller = UnaryOperator.<GenericDescriptor>identity().thenApply(GenericDescriptor::getFullName).thenApply(this::getMarshaller);
		this.type = Function.when(typeId.thenTest(Objects::nonNull), identifiedMarshaller, namedMarshaller).thenApply(BaseMarshaller::getJavaClass);
	}

	@Override
	public Stream<Class<?>> streamTypes() {
		return this.getGenericDescriptors().values().stream().map(this.type);
	}

	@Override
	public ProtobufTagMarshaller.ReadContext createReadContext(InputStream input, int length) throws IOException {
		return TagReaderImpl.newInstance(this.context, input, input.available());
	}

	@Override
	public ProtobufTagMarshaller.WriteContext createWriteContext(OutputStream output) {
		return TagWriterImpl.newInstance(this.context, output);
	}

	@Override
	public ProtoStreamTagMarshaller.SizeContext createSizeContext() {
		return ProtoStreamTagMarshaller.SizeContext.of(this.sizeWriterFactory, TagWriterImpl::getWrittenBytes);
	}

	@Override
	public ProtoStreamConfiguration getConfiguration() {
		return this.configuration;
	}

	@Override
	public <T> ProtoStreamMarshaller<T> getMarshaller(T object) {
		return (ProtoStreamMarshaller<T>) this.context.getMarshaller(object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ProtoStreamMarshaller<T> getMarshaller(String fullTypeName) {
		return (ProtoStreamMarshaller<T>) this.context.getMarshaller(fullTypeName);
	}

	@Override
	public <T> ProtoStreamMarshaller<T> getMarshaller(Class<T> clazz) {
		return (ProtoStreamMarshaller<T>) this.context.getMarshaller(clazz);
	}

	@Override
	public void registerMarshaller(ProtoStreamMarshaller<?> marshaller) {
		this.context.registerMarshaller(marshaller);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void registerMarshaller(BaseMarshaller<?> marshaller) {
		if (marshaller instanceof ProtoStreamMarshaller protostreamMarshaller) {
			this.registerMarshaller(protostreamMarshaller);
		} else if (marshaller instanceof ProtobufTagMarshaller nativeMarshaller) {
			// Adapt native ProtobufTagMarshaller to ProtoStreamMarshaller interface
			this.registerMarshaller(new ProtoStreamMarshaller<>() {
				@Override
				public Class<?> getJavaClass() {
					return nativeMarshaller.getJavaClass();
				}

				@Override
				public String getTypeName() {
					return nativeMarshaller.getTypeName();
				}

				@Override
				public Object readFrom(ProtoStreamReader reader) throws IOException {
					// Override default implementation
					// Native marshallers do not support shared references
					return this.read((ReadContext) reader);
				}

				@Override
				public void writeTo(ProtoStreamWriter writer, Object value) throws IOException {
					// Override default implementation
					// Native marshallers do not support shared references
					this.write((TagWriterImpl) ((WriteContext) writer).getWriter(), value);
				}

				@Override
				public Object read(ReadContext context) throws IOException {
					return nativeMarshaller.read(context);
				}

				@Override
				public void write(WriteContext context, Object value) throws IOException {
					nativeMarshaller.write(context, value);
				}
			});
		} else if (marshaller instanceof org.infinispan.protostream.EnumMarshaller) {
			// Adapt native EnumMarshaller to ProtoStreamMarshaller interface
			this.registerMarshaller(new EnumMarshaller<>((Class<Enum>) marshaller.getJavaClass()) {
				@Override
				public String getTypeName() {
					return marshaller.getTypeName();
				}
			});
		} else {
			// Reject MessageMarshaller implementations
			throw new IllegalArgumentException(marshaller.getTypeName());
		}
	}

	@Override
	public void registerMarshallerProvider(org.infinispan.protostream.SerializationContext.InstanceMarshallerProvider<?> provider) {
		if (!(provider instanceof SerializationContext.InstanceMarshallerProvider)) {
			throw new IllegalArgumentException();
		}
		this.context.registerMarshallerProvider(provider);
	}

	@Override
	public void unregisterMarshallerProvider(org.infinispan.protostream.SerializationContext.InstanceMarshallerProvider<?> provider) {
		if (!(provider instanceof SerializationContext.InstanceMarshallerProvider)) {
			throw new IllegalArgumentException();
		}
		this.context.unregisterMarshallerProvider(provider);
	}
}
