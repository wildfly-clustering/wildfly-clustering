/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import org.jgroups.Address;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;

/**
 * {@link SerializationContextInitializer} for this package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class JGroupsServerSerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(AddressMarshaller.INSTANCE.asMarshaller(Address.class).wrap(JChannelGroupMember.class, ChannelGroupMember::getId, JChannelGroupMember::new));
	}
}
