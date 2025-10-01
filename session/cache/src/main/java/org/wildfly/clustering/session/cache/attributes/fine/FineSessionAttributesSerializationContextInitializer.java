/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.fine;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;

/**
 * {@link SerializationContextInitializer} for this package.
 * @author Paul Ferraro
 */
public class FineSessionAttributesSerializationContextInitializer extends AbstractSerializationContextInitializer {
	/**
	 * Creates a serialization context initializer.
	 */
	public FineSessionAttributesSerializationContextInitializer() {
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(SessionAttributeMapComputeFunctionMarshaller.INSTANCE);
		context.registerMarshaller(SessionAttributeMapEntryMarshaller.INSTANCE);
	}
}
