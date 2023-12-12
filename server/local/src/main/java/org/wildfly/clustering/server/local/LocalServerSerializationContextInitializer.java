/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local;

import org.infinispan.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.FunctionalScalarMarshaller;
import org.wildfly.clustering.marshalling.protostream.Scalar;

/**
 * {@link org.infinispan.protostream.SerializationContextInitializer} for this package.
 * @author Paul Ferraro
 */
//@MetaInfServices(SerializationContextInitializer.class)
public class LocalServerSerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new FunctionalScalarMarshaller<>(DefaultLocalGroupMember.class, Scalar.STRING.cast(String.class), DefaultLocalGroupMember::getName, DefaultLocalGroupMember::new));
	}
}
