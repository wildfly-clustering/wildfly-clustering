/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.fine;

import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.ByteBufferMarshalledValue;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Validates marshalling of {@link SessionAttributeMapEntry}.
 * @author Paul Ferraro
 */
public class SessionAttributeMapEntryMarshallerTestCase {

	@Test
	public void test() {
		ProtoStreamTesterFactory factory = new ProtoStreamTesterFactory(new FineSessionAttributesSerializationContextInitializer());
		ByteBufferMarshaller marshaller = factory.getMarshaller();
		ByteBufferMarshalledValue<UUID> value = new ByteBufferMarshalledValue<>(UUID.randomUUID(), marshaller);
		Consumer<SessionAttributeMapEntry<ByteBufferMarshalledValue<UUID>>> tester = factory.createTester();
		tester.accept(new SessionAttributeMapEntry<>("foo", value));
	}
}
