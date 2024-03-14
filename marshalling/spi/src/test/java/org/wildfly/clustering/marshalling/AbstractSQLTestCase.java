/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

/**
 * Generic tests for java.sql.* classes.
 * @author Paul Ferraro
 */
public abstract class AbstractSQLTestCase {

	private final MarshallingTesterFactory factory;

	public AbstractSQLTestCase(MarshallingTesterFactory factory) {
		this.factory = factory;
	}

	@Test
	public void testSQLDate() {
		Consumer<Date> tester = this.factory.createTester();
		tester.accept(Date.valueOf(LocalDate.now()));
	}

	@Test
	public void testSQLTime() {
		Consumer<Time> tester = this.factory.createTester();
		tester.accept(Time.valueOf(LocalTime.now()));
	}

	@Test
	public void testSQLTimestamp() {
		Consumer<Timestamp> tester = this.factory.createTester();
		tester.accept(Timestamp.valueOf(LocalDateTime.now()));
	}
}
