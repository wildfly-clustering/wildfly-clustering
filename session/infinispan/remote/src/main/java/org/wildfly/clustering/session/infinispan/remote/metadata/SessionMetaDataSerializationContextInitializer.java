/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.metadata;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.session.cache.SessionKeyMarshaller;

/**
 * The serialization context initializer for this package.
 * @author Paul Ferraro
 */
public class SessionMetaDataSerializationContextInitializer extends AbstractSerializationContextInitializer {
	/**
	 * Creates a serialization context initializer.
	 */
	public SessionMetaDataSerializationContextInitializer() {
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new SessionKeyMarshaller<>(SessionCreationMetaDataKey::new));
		context.registerMarshaller(new SessionKeyMarshaller<>(SessionAccessMetaDataKey::new));
	}
}
