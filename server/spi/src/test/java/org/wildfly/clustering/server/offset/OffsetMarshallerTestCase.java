/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.offset;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;

/**
 * Unit test for {@link Offset} marshalling.
 * @author Paul Ferraro
 */
public class OffsetMarshallerTestCase {

	@ParameterizedTest
	@TesterFactorySource
	public void duration(TesterFactory factory) {
		Consumer<Offset<Duration>> tester = factory.createTester();

		tester.accept(Offset.forDuration(Duration.ZERO));
		tester.accept(Offset.forDuration(Duration.ofSeconds(1)));
	}

	@ParameterizedTest
	@TesterFactorySource
	public void instant(TesterFactory factory) {
		Consumer<Offset<Instant>> tester = factory.createTester();

		tester.accept(Offset.forInstant(Duration.ZERO));
		tester.accept(Offset.forInstant(Duration.ofSeconds(1)));
	}
}
