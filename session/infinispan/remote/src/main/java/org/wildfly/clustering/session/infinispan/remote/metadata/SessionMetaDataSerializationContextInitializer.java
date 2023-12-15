/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.metadata;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.session.cache.IdentifierMarshaller;

/**
 * @author Paul Ferraro
 */
public class SessionMetaDataSerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(IdentifierMarshaller.getKeyMarshaller(SessionCreationMetaDataKey::new));
		context.registerMarshaller(IdentifierMarshaller.getKeyMarshaller(SessionAccessMetaDataKey::new));
	}
}
