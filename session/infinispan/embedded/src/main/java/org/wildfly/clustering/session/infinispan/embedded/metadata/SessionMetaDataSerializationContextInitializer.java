/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.metadata;

import org.infinispan.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.EnumMarshaller;
import org.wildfly.clustering.session.cache.SessionKeyMarshaller;

/**
 * @author Paul Ferraro
 */
public class SessionMetaDataSerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new SessionKeyMarshaller<>(SessionMetaDataKey.class, SessionMetaDataKey::new));
		context.registerMarshaller(new EnumMarshaller<>(SessionMetaDataKeyFilter.class));
	}
}
