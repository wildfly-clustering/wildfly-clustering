/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * A serialization context initializer for this package.
 * @author Paul Ferraro
 */
public class CoarseSessionMetaDataSerializationContextInitializer extends AbstractSerializationContextInitializer {
	/**
	 * Create a serialization context initializer.
	 */
	public CoarseSessionMetaDataSerializationContextInitializer() {
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(DefaultSessionMetaDataEntryMarshaller.INSTANCE);
		context.registerMarshaller(SessionMetaDataEntryFunctionMarshaller.INSTANCE);
	}
}
