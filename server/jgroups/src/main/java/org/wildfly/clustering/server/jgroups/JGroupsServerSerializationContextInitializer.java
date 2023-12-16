/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.net.InetSocketAddress;
import java.util.List;

import org.jgroups.Address;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.UUID;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.CompositeSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;

/**
 * {@link SerializationContextInitializer} for this package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class JGroupsServerSerializationContextInitializer extends CompositeSerializationContextInitializer {

	public JGroupsServerSerializationContextInitializer() {
		super(List.of(
				new AbstractSerializationContextInitializer("org.jgroups.stack.proto", JGroupsServerSerializationContextInitializer.class) {
					@Override
					public void registerMarshallers(SerializationContext context) {
						context.registerMarshaller(context.getMarshaller(InetSocketAddress.class).wrap(IpAddress.class, address -> new InetSocketAddress(address.getIpAddress(), address.getPort()), IpAddress::new));
					}
				},
				new AbstractSerializationContextInitializer("org.jgroups.util.proto", JGroupsServerSerializationContextInitializer.class) {
					@Override
					public void registerMarshallers(SerializationContext context) {
						context.registerMarshaller(context.getMarshaller(java.util.UUID.class).wrap(UUID.class, uuid -> new java.util.UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()), uuid -> new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits())));
					}
				},
				new AbstractSerializationContextInitializer() {
					@Override
					public void registerMarshallers(SerializationContext context) {
						context.registerMarshaller(AddressMarshaller.INSTANCE.asMarshaller(Address.class).wrap(JChannelGroupMember.class, ChannelGroupMember::getAddress, JChannelGroupMember::new));
					}
				}));
	}
}
