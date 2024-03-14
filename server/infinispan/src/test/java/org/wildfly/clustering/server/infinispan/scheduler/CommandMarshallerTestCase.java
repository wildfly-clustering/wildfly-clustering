/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for marshalling scheduler commands.
 * @author Paul Ferraro
 */
public class CommandMarshallerTestCase {

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void testScheduleWithLocalMetaDataCommand(TesterFactory factory) {
		Consumer<ScheduleWithTransientMetaDataCommand<String, String>> tester = factory.createTester(this::assertEquals);

		tester.accept(new ScheduleWithTransientMetaDataCommand<>("foo"));
		tester.accept(new ScheduleWithTransientMetaDataCommand<>("foo", "bar"));
	}

	<I, M> void assertEquals(ScheduleWithTransientMetaDataCommand<I, M> expected, ScheduleWithTransientMetaDataCommand<I, M> actual) {
		Assertions.assertEquals(expected.getId(), actual.getId());
		Assertions.assertNull(actual.getMetaData());
	}

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void testCancelCommand(TesterFactory factory) {
		Consumer<CancelCommand<String, Object>> tester = factory.createTester(this::assertEquals);

		tester.accept(new CancelCommand<>("foo"));
	}

	<I, M> void assertEquals(CancelCommand<I, M> expected, CancelCommand<I, M> actual) {
		Assertions.assertEquals(expected.getId(), actual.getId());
	}

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void testScheduleWithMetaDataCommand(TesterFactory factory) {
		Consumer<ScheduleWithMetaDataCommand<String, String>> tester = factory.createTester(this::assertEquals);

		tester.accept(new ScheduleWithMetaDataCommand<>("foo", "bar"));
	}

	<I, M> void assertEquals(ScheduleWithMetaDataCommand<I, M> expected, ScheduleWithMetaDataCommand<I, M> actual) {
		Assertions.assertEquals(expected.getId(), actual.getId());
		Assertions.assertEquals(expected.getMetaData(), actual.getMetaData());
	}
}
