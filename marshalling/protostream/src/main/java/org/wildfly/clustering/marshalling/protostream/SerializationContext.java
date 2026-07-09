/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

/**
 * Extension of {@link SerializationContext} that ensures all registered marshallers implement {@link ProtoStreamMarshaller}.
 * Overrides {@link #getMarshaller(Class)} {@link #getMarshaller(String)} and {@link #getMarshaller(Object)} to return a {@link ProtoStreamMarshaller}.
 * @author Paul Ferraro
 */
public interface SerializationContext extends ImmutableSerializationContext, org.infinispan.protostream.SerializationContext {

	/**
	 * Registers a marshaller with this context.
	 * @param marshaller the marshaller to register
	 */
	void registerMarshaller(ProtoStreamMarshaller<?> marshaller);

	/**
	 * An instance marshaller provider that ensures all registered marshallers implement {@link ProtoStreamMarshaller}.
	 * @param <T> the marshalled type
	 */
	interface InstanceMarshallerProvider<T> extends org.infinispan.protostream.SerializationContext.InstanceMarshallerProvider<T> {

		@Override
		ProtoStreamMarshaller<T> getMarshaller(T instance);

		@Override
		ProtoStreamMarshaller<T> getMarshaller(String typeName);
	}
}
