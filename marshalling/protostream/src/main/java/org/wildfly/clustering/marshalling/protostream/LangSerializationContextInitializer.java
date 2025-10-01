/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

/**
 * Serialization context initializer for the {@link java.lang} package.
 * @author Paul Ferraro
 */
public class LangSerializationContextInitializer extends AbstractSerializationContextInitializer {

	private final ClassLoaderMarshaller loaderMarshaller;

	/**
	 * Creates a serialization context initializer for the the {@link java.lang} package using the specified class loader marshaller.
	 * @param loaderMarshaller a class loader marshaller
	 */
	public LangSerializationContextInitializer(ClassLoaderMarshaller loaderMarshaller) {
		super(Class.class.getPackage());
		this.loaderMarshaller = loaderMarshaller;
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new ClassMarshaller(this.loaderMarshaller));
		context.registerMarshaller(StackTraceElementMarshaller.INSTANCE);
		context.registerMarshaller(new ExceptionMarshaller<>(Throwable.class));
	}
}
