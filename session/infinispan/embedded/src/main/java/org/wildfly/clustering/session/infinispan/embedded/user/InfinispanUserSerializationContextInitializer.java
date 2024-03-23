/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.user;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.session.cache.IdentifierScalarMarshaller;

/**
 * @author Paul Ferraro
 */
public class InfinispanUserSerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(IdentifierScalarMarshaller.INSTANCE.toKeyMarshaller(UserContextKey::new));
		context.registerMarshaller(IdentifierScalarMarshaller.INSTANCE.toKeyMarshaller(UserSessionsKey::new));
	}
}
