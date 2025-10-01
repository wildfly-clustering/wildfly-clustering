/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;

import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.ImmutableSerializationContext;
import org.infinispan.protostream.ProtobufTagMarshaller;
import org.infinispan.protostream.impl.SerializationContextImpl;
import org.infinispan.protostream.impl.TagWriterImpl;

/**
 * Decorates {@link SerializationContextImpl}, ensuring that all registered marshallers implement {@link ProtoStreamMarshaller}.
 * We have to use the decorator pattern since SerializationContextImpl is final.
 * @author Paul Ferraro
 */
public class DefaultSerializationContext extends NativeSerializationContext implements SerializationContext {

	private final org.infinispan.protostream.SerializationContext context;

	/**
	 * Creates a new serialization context from the specified context
	 * @param context a decorated serialization context implementation
	 */
	public DefaultSerializationContext(org.infinispan.protostream.SerializationContext context) {
		super(context);
		this.context = context;
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
				public Class<? extends Object> getJavaClass() {
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

	@Override
	public ImmutableSerializationContext getImmutableSerializationContext() {
		return this.context;
	}
}
