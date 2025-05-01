/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;

/**
 * Unit test for {@link MapEntry} marshalling.
 * @author Paul Ferraro
 */
public class MapEntryMarshallerTestCase {

	@ParameterizedTest
	@TesterFactorySource
	public void test(TesterFactory factory) {
		Consumer<MapEntry<UUID, String>> tester = factory.createTester();

		tester.accept(MapEntry.of(null, null));
		tester.accept(MapEntry.of(UUID.randomUUID(), null));
		tester.accept(MapEntry.of(null, "foo"));
		tester.accept(MapEntry.of(UUID.randomUUID(), "bar"));
	}
}
