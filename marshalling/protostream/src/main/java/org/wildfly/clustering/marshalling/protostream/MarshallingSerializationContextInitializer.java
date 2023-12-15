/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

/**
 * @author Paul Ferraro
 */
public class MarshallingSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public MarshallingSerializationContextInitializer() {
		super("org.wildfly.clustering.marshalling.proto");
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(ByteBufferMarshalledKeyMarshaller.INSTANCE);
		context.registerMarshaller(ByteBufferMarshalledValueMarshaller.INSTANCE);
	}
}
