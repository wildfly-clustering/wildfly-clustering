/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;

/**
 * Provider of marshallers for the org.jgroups.stack package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class JGroupsStackSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public JGroupsStackSerializationContextInitializer() {
		super("org.jgroups.stack.proto");
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new IpAddressMarshaller());
	}
}
