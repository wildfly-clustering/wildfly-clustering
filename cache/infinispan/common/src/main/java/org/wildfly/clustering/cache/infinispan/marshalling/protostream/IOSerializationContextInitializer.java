/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.marshalling.protostream;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * @author Paul Ferraro
 */
public class IOSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public IOSerializationContextInitializer() {
		super("org.infinispan.commons.io.proto");
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(ByteBufferMarshaller.INSTANCE);
	}
}
