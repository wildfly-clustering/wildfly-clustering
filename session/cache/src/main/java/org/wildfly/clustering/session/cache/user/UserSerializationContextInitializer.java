/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * The serialization context initializer for this package.
 * @author Paul Ferraro
 */
public class UserSerializationContextInitializer extends AbstractSerializationContextInitializer {
	/**
	 * Create a serialization context initializer.
	 */
	public UserSerializationContextInitializer() {
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(Scalar.ANY.toMarshaller(UserContextEntry.class, UserContextEntry::getPersistentContext, UserContextEntry::new));
	}
}
