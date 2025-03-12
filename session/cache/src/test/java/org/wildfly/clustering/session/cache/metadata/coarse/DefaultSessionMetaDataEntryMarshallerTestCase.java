/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.server.offset.Offset;

/**
 * Unit test for {@link DefaultSessionMetaDataEntry} marshalling.
 * @author Paul Ferraro
 */
public class DefaultSessionMetaDataEntryMarshallerTestCase {

	@Test
	public void test() {
		Consumer<ContextualSessionMetaDataEntry<Object>> tester = new ProtoStreamTesterFactory(new CoarseSessionMetaDataSerializationContextInitializer()).createTester(DefaultSessionMetaDataEntryMarshallerTestCase::assertEquals);

		ContextualSessionMetaDataEntry<Object> entry = new DefaultSessionMetaDataEntry<>(Instant.now());

		// Default max-inactive-interval
		entry.setTimeout(Duration.ofMinutes(30));
		// Default last access duration
		entry.getLastAccessEndTime().setOffset(Offset.forInstant(Duration.ofSeconds(1)));
		tester.accept(entry);

		Instant lastAccessStartTime = Instant.now();
		entry.getLastAccessStartTime().set(lastAccessStartTime);
		entry.getLastAccessEndTime().set(lastAccessStartTime.plus(Duration.ofSeconds(2)));
		tester.accept(entry);

		// Custom max-inactive-interval
		entry.setTimeout(Duration.ofMinutes(10));
		tester.accept(entry);
	}

	static void assertEquals(ContextualSessionMetaDataEntry<Object> entry1, ContextualSessionMetaDataEntry<Object> entry2) {
		// Compare only to millisecond precision
		assertThat(entry1.getLastAccessStartTime().getBasis().toEpochMilli()).isEqualTo(entry2.getLastAccessStartTime().getBasis().toEpochMilli());
		assertThat(entry2.getTimeout()).isEqualTo(entry1.getTimeout());
		assertThat(entry2.getLastAccessStartTime().get()).isEqualTo(entry1.getLastAccessStartTime().get());
		assertThat(entry2.getLastAccessEndTime().get()).isEqualTo(entry1.getLastAccessEndTime().get());
	}
}
