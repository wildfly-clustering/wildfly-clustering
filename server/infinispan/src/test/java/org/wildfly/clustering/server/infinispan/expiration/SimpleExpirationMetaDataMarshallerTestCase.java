/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.expiration;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

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
		Consumer<ExpirationMetaData> tester = factory.createTester((expected, actual) -> {
			assertThat(actual.getMaxIdle()).isEqualTo(expected.getMaxIdle());
			assertThat(actual.getLastAccessTime()).isEqualTo(expected.getLastAccessTime());
		});

		tester.accept(new SimpleExpirationMetaData(Optional.empty(), Optional.empty()));
		tester.accept(new SimpleExpirationMetaData(Optional.of(Duration.ofMinutes(30)), Optional.empty()));
		tester.accept(new SimpleExpirationMetaData(Optional.empty(), Optional.of(Instant.EPOCH)));
		tester.accept(new SimpleExpirationMetaData(Optional.of(Duration.ofMinutes(30)), Optional.of(Instant.EPOCH)));
		tester.accept(new SimpleExpirationMetaData(Optional.of(Duration.ofSeconds(600)), Optional.of(Instant.now())));
	}
}
