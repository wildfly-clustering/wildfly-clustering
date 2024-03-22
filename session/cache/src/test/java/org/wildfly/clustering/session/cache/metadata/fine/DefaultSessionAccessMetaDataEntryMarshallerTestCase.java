/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for {@link DefaultSessionAccessMetaDataEntry} marshalling.
 * @author Paul Ferraro
 */
public class DefaultSessionAccessMetaDataEntryMarshallerTestCase {

	@Test
	public void test() {
		Consumer<DefaultSessionAccessMetaDataEntry> tester = new ProtoStreamTesterFactory(new FineSessionMetaDataSerializationContextInitializer()).createTester(DefaultSessionAccessMetaDataEntryMarshallerTestCase::assertEquals);

		DefaultSessionAccessMetaDataEntry metaData = new DefaultSessionAccessMetaDataEntry();

		// New session
		metaData.setLastAccessDuration(Duration.ZERO, Duration.ofNanos(100_000_000));
		tester.accept(metaData);

		// Existing session, sub-second response time
		metaData.setLastAccessDuration(Duration.ofSeconds(60 * 5), Duration.ofNanos(100_000_000));
		tester.accept(metaData);

		// Existing session, +1 second response time
		metaData.setLastAccessDuration(Duration.ofSeconds(60 * 5), Duration.ofSeconds(1, 100_000_000));
		tester.accept(metaData);
	}

	static void assertEquals(DefaultSessionAccessMetaDataEntry metaData1, DefaultSessionAccessMetaDataEntry metaData2) {
		Assertions.assertEquals(metaData1.getSinceCreationDuration(), metaData2.getSinceCreationDuration());
		Assertions.assertEquals(metaData1.getLastAccessDuration(), metaData2.getLastAccessDuration());
	}
}
