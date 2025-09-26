/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

/**
 * Serialization context initializer for this package.
 * @author Paul Ferraro
 */
public class AnySerializationContextInitializer extends AbstractSerializationContextInitializer {

	/**
	 * Creates a new serialization context initializer.
	 */
	public AnySerializationContextInitializer() {
		// Do nothing
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(AnyMarshaller.INSTANCE);
	}
}
