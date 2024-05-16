/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.infinispan.protostream.ImmutableSerializationContext;

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

	ImmutableSerializationContext getImmutableSerializationContext();

	interface InstanceMarshallerProvider<T> extends org.infinispan.protostream.SerializationContext.InstanceMarshallerProvider<T> {

		@Override
		ProtoStreamMarshaller<T> getMarshaller(T instance);

		@Override
		ProtoStreamMarshaller<T> getMarshaller(String typeName);
	}
}
