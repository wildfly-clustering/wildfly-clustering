/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.infinispan.expiration.ScheduleWithExpirationMetaDataCommand;

/**
 * Unit test for marshalling scheduler commands.
 * @author Paul Ferraro
 */
public class CommandMarshallerTestCase {

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void testScheduleWithLocalMetaDataCommand(TesterFactory factory) {
		Consumer<ScheduleCommand<String, String>> tester = factory.createTester((expected, actual) -> {
			assertThat(actual.getId()).isEqualTo(expected.getId());
			assertThat(actual.getPersistentMetaData()).isEqualTo(expected.getPersistentMetaData());
		});

		tester.accept(new ScheduleWithTransientMetaDataCommand<>("foo", null));
		tester.accept(new ScheduleWithTransientMetaDataCommand<>("foo", "bar"));
	}

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void testCancelCommand(TesterFactory factory) {
		Consumer<CancelCommand<String, Object>> tester = factory.createTester((expected, actual) -> {
			assertThat(actual.getId()).isEqualTo(expected.getId());
		});

		tester.accept(new CancelCommand<>("foo"));
	}

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void testScheduleWithMetaDataCommand(TesterFactory factory) {
		Consumer<ScheduleCommand<String, String>> tester = factory.createTester((expected, actual) -> {
			assertThat(actual.getId()).isEqualTo(expected.getId());
			assertThat(actual.getPersistentMetaData()).isEqualTo(expected.getPersistentMetaData());
		});

		tester.accept(new ScheduleWithPersistentMetaDataCommand<>("foo", "bar"));
	}

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void testScheduleWithExpirationMetaDataCommand(TesterFactory factory) {
		Consumer<ScheduleCommand<String, ExpirationMetaData>> tester = factory.createTester((expected, actual) -> {
			assertThat(actual.getId()).isEqualTo(expected.getId());
			assertThat(actual.getPersistentMetaData().getTimeout()).isEqualTo(expected.getPersistentMetaData().getTimeout());
			assertThat(actual.getPersistentMetaData().getLastAccessTime()).isEqualTo(expected.getPersistentMetaData().getLastAccessTime());
		});

		tester.accept(new ScheduleWithExpirationMetaDataCommand<>("foo", new TestExpirationMetaData(Duration.ZERO, Instant.EPOCH)));
		tester.accept(new ScheduleWithExpirationMetaDataCommand<>("bar", new TestExpirationMetaData(Duration.ofMinutes(30), Instant.now())));
		tester.accept(new ScheduleWithExpirationMetaDataCommand<>("bar", new TestExpirationMetaData(Duration.ofHours(1), Instant.now())));
	}

	private static class TestExpirationMetaData implements ExpirationMetaData {
		private final Duration timeout;
		private final Instant lastAccessTime;

		TestExpirationMetaData(Duration timeout, Instant lastAccessTime) {
			this.timeout = timeout;
			this.lastAccessTime = lastAccessTime;
		}

		@Override
		public Duration getTimeout() {
			return this.timeout;
		}

		@Override
		public Instant getLastAccessTime() {
			return this.lastAccessTime;
		}
	}
}
