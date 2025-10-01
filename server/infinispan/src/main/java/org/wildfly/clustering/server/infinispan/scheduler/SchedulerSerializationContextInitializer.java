/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;

/**
 * The serialization context initializer for this package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class SchedulerSerializationContextInitializer extends AbstractSerializationContextInitializer {

	/**
	 * Creates a serialization context initializer.
	 */
	public SchedulerSerializationContextInitializer() {
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(Scalar.ANY.toMarshaller(CancelCommand.class, CancelCommand::getId, CancelCommand::new));
		context.registerMarshaller(Scalar.ANY.toMarshaller(ContainsCommand.class, ContainsCommand::getId, ContainsCommand::new));
		context.registerMarshaller(ScheduleCommandMarshaller.INSTANCE);
	}
}
