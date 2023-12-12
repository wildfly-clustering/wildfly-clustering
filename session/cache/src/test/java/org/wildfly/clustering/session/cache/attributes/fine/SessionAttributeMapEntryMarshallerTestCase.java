/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.fine;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.ByteBufferMarshalledValue;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshallingTester;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Validates marshalling of {@link SessionAttributeMapEntry}.
 * @author Paul Ferraro
 */
public class SessionAttributeMapEntryMarshallerTestCase {

	@Test
	public void test() throws IOException {
		ProtoStreamTesterFactory factory = new ProtoStreamTesterFactory(List.of(new FineSessionAttributesSerializationContextInitializer()));
		ByteBufferMarshaller marshaller = factory.get();
		ByteBufferMarshalledValue<UUID> value = new ByteBufferMarshalledValue<>(UUID.randomUUID(), marshaller);
		MarshallingTester<SessionAttributeMapEntry<ByteBufferMarshalledValue<UUID>>> tester = factory.createTester();
		tester.test(new SessionAttributeMapEntry<>("foo", value));
	}
}
