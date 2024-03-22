/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.offset;

import java.time.Duration;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;

/**
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class OffsetSerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		ProtoStreamMarshaller<Duration> marshaller = context.getMarshaller(Duration.class);
		context.registerMarshaller(marshaller.wrap(Offset.DurationOffset.class, Offset.DurationOffset::get, Offset.DurationOffset::new));
		context.registerMarshaller(marshaller.wrap(Offset.InstantOffset.class, Offset.InstantOffset::get, Offset.InstantOffset::new));
	}
}
