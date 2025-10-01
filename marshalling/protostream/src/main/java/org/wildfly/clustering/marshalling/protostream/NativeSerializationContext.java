/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.util.Map;
import java.util.Set;

import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.DescriptorParserException;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.config.Configuration;
import org.infinispan.protostream.descriptors.Descriptor;
import org.infinispan.protostream.descriptors.EnumDescriptor;
import org.infinispan.protostream.descriptors.FileDescriptor;
import org.infinispan.protostream.descriptors.GenericDescriptor;

/**
 * Native serialization context decorator.
 * We have to use the decorator pattern since SerializationContextImpl is final.
 * @author Paul Ferraro
 */
public class NativeSerializationContext implements SerializationContext {

	private final SerializationContext context;

	/**
	 * Constructs a new native serialization context decorator
	 * @param context a serialization context
	 */
	public NativeSerializationContext(SerializationContext context) {
		this.context = context;
	}

	@Override
	public Configuration getConfiguration() {
		return this.context.getConfiguration();
	}

	@Override
	public Map<String, FileDescriptor> getFileDescriptors() {
		return this.context.getFileDescriptors();
	}

	@Override
	public Map<String, GenericDescriptor> getGenericDescriptors() {
		return this.context.getGenericDescriptors();
	}

	@Override
	public Descriptor getMessageDescriptor(String fullTypeName) {
		return this.context.getMessageDescriptor(fullTypeName);
	}

	@Override
	public EnumDescriptor getEnumDescriptor(String fullTypeName) {
		return this.context.getEnumDescriptor(fullTypeName);
	}

	@Override
	public boolean canMarshall(Class<?> javaClass) {
		return this.context.canMarshall(javaClass);
	}

	@Override
	public boolean canMarshall(String fullTypeName) {
		return this.context.canMarshall(fullTypeName);
	}

	@Override
	public boolean canMarshall(Object object) {
		return this.context.canMarshall(object);
	}

	@Override
	public <T> BaseMarshaller<T> getMarshaller(T object) {
		return this.context.getMarshaller(object);
	}

	@Override
	public <T> BaseMarshaller<T> getMarshaller(String fullTypeName) {
		return this.context.getMarshaller(fullTypeName);
	}

	@Override
	public <T> BaseMarshaller<T> getMarshaller(Class<T> clazz) {
		return this.context.getMarshaller(clazz);
	}

	@Deprecated
	@Override
	public String getTypeNameById(Integer typeId) {
		return this.context.getTypeNameById(typeId);
	}

	@Deprecated
	@Override
	public Integer getTypeIdByName(String fullTypeName) {
		return this.context.getTypeIdByName(fullTypeName);
	}

	@Override
	public GenericDescriptor getDescriptorByTypeId(Integer typeId) {
		return this.context.getDescriptorByTypeId(typeId);
	}

	@Override
	public GenericDescriptor getDescriptorByName(String fullTypeName) {
		return this.context.getDescriptorByName(fullTypeName);
	}

	@Override
	public void registerProtoFiles(FileDescriptorSource source) throws DescriptorParserException {
		this.context.registerProtoFiles(source);
	}

	@Override
	public void unregisterProtoFile(String fileName) {
		this.context.unregisterProtoFile(fileName);
	}

	@Override
	public void unregisterProtoFiles(Set<String> fileNames) {
		this.context.unregisterProtoFiles(fileNames);
	}

	@Override
	public void registerMarshaller(BaseMarshaller<?> marshaller) {
		this.context.registerMarshaller(marshaller);
	}

	@Override
	public void unregisterMarshaller(BaseMarshaller<?> marshaller) {
		this.context.unregisterMarshaller(marshaller);
	}

	@Deprecated
	@Override
	public void registerMarshallerProvider(MarshallerProvider provider) {
		this.context.registerMarshallerProvider(provider);
	}

	@Deprecated
	@Override
	public void unregisterMarshallerProvider(MarshallerProvider provider) {
		this.context.unregisterMarshallerProvider(provider);
	}

	@Override
	public void registerMarshallerProvider(org.infinispan.protostream.SerializationContext.InstanceMarshallerProvider<?> provider) {
		this.context.registerMarshallerProvider(provider);
	}

	@Override
	public void unregisterMarshallerProvider(org.infinispan.protostream.SerializationContext.InstanceMarshallerProvider<?> provider) {
		this.context.unregisterMarshallerProvider(provider);
	}
}
