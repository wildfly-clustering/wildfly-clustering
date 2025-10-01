/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.attributes;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.session.cache.SessionKeyMarshaller;

/**
 * The serialization context initializer for this module
 * @author Paul Ferraro
 */
public class SessionAttributesSerializationContextInitializer extends AbstractSerializationContextInitializer {
	/**
	 * Creates a serialization context initializer.
	 */
	public SessionAttributesSerializationContextInitializer() {
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new SessionKeyMarshaller<>(SessionAttributesKey::new));
	}
}
