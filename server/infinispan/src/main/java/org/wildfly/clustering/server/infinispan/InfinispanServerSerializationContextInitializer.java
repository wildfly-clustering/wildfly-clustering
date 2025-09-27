/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;

/**
 * {@link org.infinispan.protostream.SerializationContextInitializer} for this package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class InfinispanServerSerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(context.getMarshaller(JGroupsAddress.class).wrap(EmbeddedCacheManagerGroupMember.class, EmbeddedCacheManagerGroupMember::getId, EmbeddedCacheManagerGroupMember::new));
		context.registerMarshaller(Scalar.STRING.cast(String.class).toMarshaller(LocalEmbeddedCacheManagerGroupMember.class, LocalEmbeddedCacheManagerGroupMember::getName, LocalEmbeddedCacheManagerGroupMember::new));
	}
}
