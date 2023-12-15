/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;

import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.ImmutableSerializationContext;
import org.infinispan.protostream.ProtobufTagMarshaller;
import org.infinispan.protostream.impl.TagWriterImpl;

/**
 * Extension of {@link SerializationContext} that ensures all registered marshallers implement {@link ProtoStreamMarshaller}.
 * Overrides {@link #getMarshaller(Class)} {@link #getMarshaller(String)} and {@link #getMarshaller(Object)} to return a {@link ProtoStreamMarshaller}.
 * @author Paul Ferraro
 */
public interface SerializationContext extends org.infinispan.protostream.SerializationContext {

	void registerMarshaller(ProtoStreamMarshaller<?> marshaller);

	@Override
	<T> ProtoStreamMarshaller<T> getMarshaller(Class<T> targetClass);

	@Override
	<T> ProtoStreamMarshaller<T> getMarshaller(T object);

	@Override
	<T> ProtoStreamMarshaller<T> getMarshaller(String fullTypeName);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default void registerMarshaller(BaseMarshaller<?> marshaller) {
		if (marshaller instanceof ProtoStreamMarshaller) {
			this.registerMarshaller((ProtoStreamMarshaller<?>) marshaller);
		} else if (marshaller instanceof ProtobufTagMarshaller) {
			// Adapt native ProtobufTagMarshaller to ProtoStreamMarshaller interface
			ProtobufTagMarshaller<Object> nativeMarshaller = (ProtobufTagMarshaller<Object>) marshaller;
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

	default ImmutableSerializationContext getImmutableSerializationContext() {
		return this;
	}

	interface InstanceMarshallerProvider<T> extends org.infinispan.protostream.SerializationContext.InstanceMarshallerProvider<T> {

		@Override
		ProtoStreamMarshaller<T> getMarshaller(T instance);

		@Override
		ProtoStreamMarshaller<T> getMarshaller(String typeName);
	}
}
