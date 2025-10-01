/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * Serialization context initializer for this package.
 * @author Paul Ferraro
 */
public class FineSessionMetaDataSerializationContextInitializer extends AbstractSerializationContextInitializer {
	/**
	 * Creates a serialization context initializer.
	 */
	public FineSessionMetaDataSerializationContextInitializer() {
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(DefaultSessionCreationMetaDataEntryMarshaller.INSTANCE);
		context.registerMarshaller(DefaultSessionAccessMetaDataEntryMarshaller.INSTANCE);
	}
}
