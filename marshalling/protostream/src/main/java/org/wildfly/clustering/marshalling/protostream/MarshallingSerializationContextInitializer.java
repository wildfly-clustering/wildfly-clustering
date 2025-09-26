/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.wildfly.clustering.marshalling.ByteBufferMarshaller;

/**
 * Serialization context initializer for the {@link org.wildfly.clustering.marshalling} package.
 * @author Paul Ferraro
 */
public class MarshallingSerializationContextInitializer extends AbstractSerializationContextInitializer {

	/**
	 * Constructs a new serialization context initializer.
	 */
	public MarshallingSerializationContextInitializer() {
		super(ByteBufferMarshaller.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(ByteBufferMarshalledKeyMarshaller.INSTANCE);
		context.registerMarshaller(ByteBufferMarshalledValueMarshaller.INSTANCE);
	}
}
