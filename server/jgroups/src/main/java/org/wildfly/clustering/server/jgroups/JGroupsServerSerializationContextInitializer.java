/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.jgroups.Address;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.FieldSetProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.FunctionalMarshaller;

/**
 * {@link SerializationContextInitializer} for this package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class JGroupsServerSerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new FunctionalMarshaller<>(JChannelGroupMember.class, new FieldSetProtoStreamMarshaller<>(Address.class, AddressMarshaller.INSTANCE), ChannelGroupMember::getAddress, JChannelGroupMember::new));
	}
}
