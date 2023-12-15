/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.util.List;

import org.infinispan.remoting.transport.LocalModeAddress;
import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.Address;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.CompositeSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;
import org.wildfly.clustering.server.jgroups.AddressMarshaller;

/**
 * {@link org.infinispan.protostream.SerializationContextInitializer} for this package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class InfinispanServerSerializationContextInitializer extends CompositeSerializationContextInitializer {

	public InfinispanServerSerializationContextInitializer() {
		super(List.of(
				new AbstractSerializationContextInitializer("org.infinispan.remoting.transport.proto", InfinispanServerSerializationContextInitializer.class) {
					@Override
					public void registerMarshallers(SerializationContext context) {
						context.registerMarshaller(ProtoStreamMarshaller.of(LocalModeAddress.INSTANCE));
					}
				},
				new AbstractSerializationContextInitializer("org.infinispan.remoting.transport.jgroups.proto", InfinispanServerSerializationContextInitializer.class) {
					@Override
					public void registerMarshallers(SerializationContext context) {
						context.registerMarshaller(AddressMarshaller.INSTANCE.asMarshaller(Address.class).map(JGroupsAddress.class, JGroupsAddress::getJGroupsAddress, JGroupsAddress::new));
					}
				},
				new AbstractSerializationContextInitializer() {
					@Override
					public void registerMarshallers(SerializationContext context) {
						context.registerMarshaller(context.getMarshaller(JGroupsAddress.class).map(EmbeddedCacheManagerGroupMember.class, EmbeddedCacheManagerGroupMember::getAddress, EmbeddedCacheManagerGroupMember::new));
						context.registerMarshaller(Scalar.STRING.cast(String.class).toMarshaller(LocalEmbeddedCacheManagerGroupMember.class, LocalEmbeddedCacheManagerGroupMember::getName, LocalEmbeddedCacheManagerGroupMember::new));
					}
				}));
	}
}
