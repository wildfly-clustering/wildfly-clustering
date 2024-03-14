/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.expiration;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;

/**
 * Marshaller test for {@link SimpleExpirationMetaData}.
 * @author Paul Ferraro
 */
public class SimpleExpirationMetaDataMarshallerTestCase {

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void test(TesterFactory factory) {
		Consumer<ExpirationMetaData> tester = factory.createTester(SimpleExpirationMetaDataMarshallerTestCase::assertEquals);

		tester.accept(new SimpleExpirationMetaData(Duration.ofMinutes(30), Instant.EPOCH));
		tester.accept(new SimpleExpirationMetaData(Duration.ofSeconds(600), Instant.now()));
	}

	private static void assertEquals(ExpirationMetaData expected, ExpirationMetaData actual) {
		Assertions.assertEquals(expected.getTimeout(), actual.getTimeout());
		Assertions.assertEquals(expected.getLastAccessTime(), actual.getLastAccessTime());
	}
}
