/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for {@link DefaultSessionCreationMetaDataEntry} marshalling.
 * @author Paul Ferraro
 */
public class DefaultSessionCreationMetaDataEntryMarshallerTestCase {

	@Test
	public void test() {
		Consumer<DefaultSessionCreationMetaDataEntry<Object>> tester = new ProtoStreamTesterFactory(List.of(new FineSessionMetaDataSerializationContextInitializer())).createTester(DefaultSessionCreationMetaDataEntryMarshallerTestCase::assertEquals);

		DefaultSessionCreationMetaDataEntry<Object> entry = new DefaultSessionCreationMetaDataEntry<>(Instant.now());

		// Default max-inactive-interval
		entry.setTimeout(Duration.ofMinutes(30));
		tester.accept(entry);

		// Custom max-inactive-interval
		entry.setTimeout(Duration.ofMinutes(10));
		tester.accept(entry);
	}

	static void assertEquals(DefaultSessionCreationMetaDataEntry<Object> entry1, DefaultSessionCreationMetaDataEntry<Object> entry2) {
		// Compare only to millisecond precision
		Assertions.assertEquals(entry1.getCreationTime().toEpochMilli(), entry2.getCreationTime().toEpochMilli());
		Assertions.assertEquals(entry1.getTimeout(), entry2.getTimeout());
	}
}
