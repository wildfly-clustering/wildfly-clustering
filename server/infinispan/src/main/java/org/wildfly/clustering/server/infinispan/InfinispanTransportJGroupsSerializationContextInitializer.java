/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.Address;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.FieldSetProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.FunctionalMarshaller;
import org.wildfly.clustering.server.jgroups.AddressMarshaller;

/**
 * Provider of marshallers for the org.infinispan.remoting.transport.jgroups package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class InfinispanTransportJGroupsSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public InfinispanTransportJGroupsSerializationContextInitializer() {
		super("org.infinispan.remoting.transport.jgroups.proto");
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new FunctionalMarshaller<>(JGroupsAddress.class, new FieldSetProtoStreamMarshaller<>(Address.class, AddressMarshaller.INSTANCE), JGroupsAddress::getJGroupsAddress, JGroupsAddress::new));
	}
}
