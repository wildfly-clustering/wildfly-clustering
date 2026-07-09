/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.lang.invoke;

import java.lang.invoke.SerializedLambda;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * Serialization context initializer for the {@link java.lang.invoke} package.
 * @author Paul Ferraro
 */
public class LangInvokeSerializationContextInitializer extends AbstractSerializationContextInitializer {

	/**
	 * Creates a serialization context initializer for the the {@link java.lang.invoke} package.
	 */
	public LangInvokeSerializationContextInitializer() {
		super(SerializedLambda.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(SerializedLambdaMarshaller.INSTANCE);
	}
}
