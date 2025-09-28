/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;
import org.jgroups.util.UUID;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;

/**
 * {@link SerializationContextInitializer} for the {@code org.jgroups.util} package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class JGroupsUtilSerializationContextInitializer extends AbstractSerializationContextInitializer {

	/**
	 * Creates a serialization context initializer.
	 */
	public JGroupsUtilSerializationContextInitializer() {
		super(UUID.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(context.getMarshaller(java.util.UUID.class).wrap(UUID.class, uuid -> new java.util.UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()), uuid -> new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits())));
	}
}
