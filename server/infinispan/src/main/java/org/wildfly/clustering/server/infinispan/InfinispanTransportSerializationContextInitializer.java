/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.Address;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;

/**
 * {@link org.infinispan.protostream.SerializationContextInitializer} for the {@code org.infinispan.remoting.transport} package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class InfinispanTransportSerializationContextInitializer extends AbstractSerializationContextInitializer {
	/**
	 * Creates a serialization context initializer.
	 */
	public InfinispanTransportSerializationContextInitializer() {
		super(Address.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(AddressMarshaller.INSTANCE);
		context.registerMarshaller(VersionMarshaller.INSTANCE);
	}
}
