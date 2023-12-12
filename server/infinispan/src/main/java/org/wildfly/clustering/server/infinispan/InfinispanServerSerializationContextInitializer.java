/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.FunctionalMarshaller;
import org.wildfly.clustering.marshalling.protostream.FunctionalScalarMarshaller;
import org.wildfly.clustering.marshalling.protostream.Scalar;

/**
 * {@link org.infinispan.protostream.SerializationContextInitializer} for this package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class InfinispanServerSerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new FunctionalMarshaller<>(EmbeddedCacheManagerGroupMember.class, JGroupsAddress.class, EmbeddedCacheManagerGroupMember::getAddress, EmbeddedCacheManagerGroupMember::new));
		context.registerMarshaller(new FunctionalScalarMarshaller<>(LocalEmbeddedCacheManagerGroupMember.class, Scalar.STRING.cast(String.class), LocalEmbeddedCacheManagerGroupMember::getName, LocalEmbeddedCacheManagerGroupMember::new));
	}
}
