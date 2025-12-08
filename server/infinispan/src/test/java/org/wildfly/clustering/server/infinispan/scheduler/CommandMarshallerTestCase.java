/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.infinispan.expiration.ScheduleExpirationCommand;

/**
 * Unit test for marshalling scheduler commands.
 * @author Paul Ferraro
 */
public class CommandMarshallerTestCase {

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void testScheduleCommand(TesterFactory factory) {
		Consumer<ScheduleCommand<String, String>> tester = factory.createTester((expected, actual) -> {
			assertThat(actual.getKey()).isEqualTo(expected.getKey());
			assertThat(actual.getValue()).isEqualTo(expected.getValue());
		});

		tester.accept(new ScheduleCommand<>(Map.entry("foo", "bar")));
	}

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void testCancelCommand(TesterFactory factory) {
		Consumer<CancelCommand<String, Object>> tester = factory.createTester((expected, actual) -> {
			assertThat(actual.getKey()).isEqualTo(expected.getKey());
		});

		tester.accept(new CancelCommand<>("foo"));
	}

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void testScheduleWithExpirationMetaDataCommand(TesterFactory factory) {
		Consumer<ScheduleCommand<String, ExpirationMetaData>> tester = factory.createTester((expected, actual) -> {
			assertThat(actual.getKey()).isEqualTo(expected.getKey());
			assertThat(actual.getValue().getMaxIdle()).isEqualTo(expected.getValue().getMaxIdle());
			assertThat(actual.getValue().getLastAccessTime()).isEqualTo(expected.getValue().getLastAccessTime());
		});

		tester.accept(new ScheduleExpirationCommand<>(Map.entry("foo", new TestExpirationMetaData(null, null))));
		tester.accept(new ScheduleExpirationCommand<>(Map.entry("foo", new TestExpirationMetaData(Duration.ZERO, Instant.EPOCH))));
		tester.accept(new ScheduleExpirationCommand<>(Map.entry("bar", new TestExpirationMetaData(Duration.ofMinutes(30), Instant.now()))));
		tester.accept(new ScheduleExpirationCommand<>(Map.entry("bar", new TestExpirationMetaData(Duration.ofHours(1), Instant.now()))));
	}

	private static class TestExpirationMetaData implements ExpirationMetaData {
		private final Duration timeout;
		private final Instant lastAccessTime;

		TestExpirationMetaData(Duration timeout, Instant lastAccessTime) {
			this.timeout = timeout;
			this.lastAccessTime = lastAccessTime;
		}

		@Override
		public Optional<Duration> getMaxIdle() {
			return Optional.ofNullable(this.timeout);
		}

		@Override
		public Optional<Instant> getLastAccessTime() {
			return Optional.ofNullable(this.lastAccessTime);
		}
	}
}
