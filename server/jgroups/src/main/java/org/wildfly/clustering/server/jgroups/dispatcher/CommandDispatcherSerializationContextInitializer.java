/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher;

import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.EnumMarshaller;

/**
 * {@link SerializationContextInitializer} for this package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class CommandDispatcherSerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new EnumMarshaller<>(ServiceResponse.class));
	}
}
