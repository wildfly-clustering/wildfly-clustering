/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.MarshallingTester;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for {@link DefaultSessionAccessMetaDataEntry} marshalling.
 * @author Paul Ferraro
 */
public class DefaultSessionAccessMetaDataEntryMarshallerTestCase {

	@Test
	public void test() throws IOException {
		MarshallingTester<DefaultSessionAccessMetaDataEntry> tester = new ProtoStreamTesterFactory(List.of(new FineSessionMetaDataSerializationContextInitializer())).createTester();

		DefaultSessionAccessMetaDataEntry metaData = new DefaultSessionAccessMetaDataEntry();

		// New session
		metaData.setLastAccessDuration(Duration.ZERO, Duration.ofNanos(100_000_000));
		tester.test(metaData, DefaultSessionAccessMetaDataEntryMarshallerTestCase::assertEquals);

		// Existing session, sub-second response time
		metaData.setLastAccessDuration(Duration.ofSeconds(60 * 5), Duration.ofNanos(100_000_000));
		tester.test(metaData, DefaultSessionAccessMetaDataEntryMarshallerTestCase::assertEquals);

		// Existing session, +1 second response time
		metaData.setLastAccessDuration(Duration.ofSeconds(60 * 5), Duration.ofSeconds(1, 100_000_000));
		tester.test(metaData, DefaultSessionAccessMetaDataEntryMarshallerTestCase::assertEquals);
	}

	static void assertEquals(DefaultSessionAccessMetaDataEntry metaData1, DefaultSessionAccessMetaDataEntry metaData2) {
		Assertions.assertEquals(metaData1.getSinceCreationDuration(), metaData2.getSinceCreationDuration());
		Assertions.assertEquals(metaData1.getLastAccessDuration(), metaData2.getLastAccessDuration());
	}
}
