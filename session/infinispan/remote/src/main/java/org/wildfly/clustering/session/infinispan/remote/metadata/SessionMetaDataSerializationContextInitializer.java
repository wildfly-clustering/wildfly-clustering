/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.metadata;

import org.infinispan.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.session.cache.SessionKeyMarshaller;

/**
 * @author Paul Ferraro
 */
public class SessionMetaDataSerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new SessionKeyMarshaller<>(SessionCreationMetaDataKey.class, SessionCreationMetaDataKey::new));
		context.registerMarshaller(new SessionKeyMarshaller<>(SessionAccessMetaDataKey.class, SessionAccessMetaDataKey::new));
	}
}
