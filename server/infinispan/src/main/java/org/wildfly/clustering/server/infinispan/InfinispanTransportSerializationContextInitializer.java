/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.LocalModeAddress;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;

/**
 * {@link org.infinispan.protostream.SerializationContextInitializer} for the {@code org.infinispan.remoting.transport} package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class InfinispanTransportSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public InfinispanTransportSerializationContextInitializer() {
		super(LocalModeAddress.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(ProtoStreamMarshaller.of(LocalModeAddress.INSTANCE));
	}
}
