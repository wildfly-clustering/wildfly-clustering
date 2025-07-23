/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.Address;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;
import org.wildfly.clustering.server.jgroups.AddressMarshaller;

/**
 * {@link org.infinispan.protostream.SerializationContextInitializer} for the {@code org.infinispan.remoting.transport.jgroups} package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class InfinispanJGroupsTransportSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public InfinispanJGroupsTransportSerializationContextInitializer() {
		super(JGroupsAddress.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(AddressMarshaller.INSTANCE.asMarshaller(Address.class).wrap(JGroupsAddress.class, JGroupsAddress::getJGroupsAddress, JGroupsAddress::new));
	}
}
