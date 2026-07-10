/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.lang;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * Serialization context initializer for the {@link java.lang} package.
 * @author Paul Ferraro
 */
public class LangSerializationContextInitializer extends AbstractSerializationContextInitializer {

	/**
	 * Creates a serialization context initializer for the the {@link java.lang} package using the specified class loader marshaller.
	 */
	public LangSerializationContextInitializer() {
		super(Class.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new ClassMarshaller(context.getConfiguration().getClassLoaderResolver()));
		context.registerMarshaller(StackTraceElementMarshaller.INSTANCE);
		context.registerMarshaller(new ExceptionMarshaller<>(Throwable.class));
	}
}
