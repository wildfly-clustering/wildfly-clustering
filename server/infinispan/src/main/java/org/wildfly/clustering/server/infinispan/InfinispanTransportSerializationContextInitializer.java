/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ValueMarshaller;

/**
 * Provider of marshallers for the org.infinispan.remoting.transport package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class InfinispanTransportSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public InfinispanTransportSerializationContextInitializer() {
		super("org.infinispan.remoting.transport.proto");
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new ValueMarshaller<>(LocalModeAddress.INSTANCE));
	}
}
