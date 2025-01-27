/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * @author Paul Ferraro
 */
public class UtilSerializationContextInitializer extends AbstractSerializationContextInitializer {

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new PropertyMarshaller<>(Property::new));
		context.registerMarshaller(new StringKeyMapEntryMarshaller<>(StringKeyMapEntry::new));
	}
}
