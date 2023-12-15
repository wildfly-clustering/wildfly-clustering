/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

/**
 * Initializer that registers protobuf schema and marshaller for {@link Any}.
 * @author Paul Ferraro
 */
public class AnySerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(AnyMarshaller.INSTANCE);
	}
}
