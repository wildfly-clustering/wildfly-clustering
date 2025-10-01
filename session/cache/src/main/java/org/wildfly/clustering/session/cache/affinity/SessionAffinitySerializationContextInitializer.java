/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * Create a serialization context initializer for this package.
 * @author Paul Ferraro
 */
public class SessionAffinitySerializationContextInitializer extends AbstractSerializationContextInitializer {
	/**
	 * Creates a serialization context initializer.
	 */
	public SessionAffinitySerializationContextInitializer() {
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(Scalar.STRING.cast(String.class).toMarshaller(SessionAffinityRegistryEntry.class, SessionAffinityRegistryEntry::getKey, SessionAffinityRegistryEntry::new));
	}
}
