/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.expiration;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;

/**
 * Marshaller test for {@link SimpleExpirationMetaData}.
 * @author Paul Ferraro
 */
public class SimpleExpirationMetaDataMarshallerTestCase {

	@Test
	public void test() throws IOException {
		Tester<ExpirationMetaData> tester = new ProtoStreamTesterFactory().createTester();

		tester.test(new SimpleExpirationMetaData(Duration.ofMinutes(30), Instant.EPOCH), this::assertEquals);
		tester.test(new SimpleExpirationMetaData(Duration.ofSeconds(600), Instant.now()), this::assertEquals);
	}

	private void assertEquals(ExpirationMetaData expected, ExpirationMetaData actual) {
		Assertions.assertEquals(expected.getTimeout(), actual.getTimeout());
		Assertions.assertEquals(expected.getLastAccessTime(), actual.getLastAccessTime());
	}
}
