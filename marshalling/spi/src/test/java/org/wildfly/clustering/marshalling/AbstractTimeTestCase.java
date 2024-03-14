/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Random;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

/**
 * Generic tests for java.time.* classes.
 * @author Paul Ferraro
 */
public abstract class AbstractTimeTestCase {

	private final MarshallingTesterFactory factory;

	public AbstractTimeTestCase(MarshallingTesterFactory factory) {
		this.factory = factory;
	}

	@Test
	public void testDayOfWeek() {
		this.factory.createTester(DayOfWeek.class).run();
	}

	@Test
	public void testDuration() {
		Consumer<Duration> tester = this.factory.createTester();
		tester.accept(Duration.between(Instant.EPOCH, Instant.now()));
		tester.accept(Duration.ofMillis(1234567890));
		tester.accept(Duration.ofSeconds(100));
		tester.accept(Duration.ZERO);
		for (ChronoUnit unit : EnumSet.of(ChronoUnit.NANOS, ChronoUnit.MICROS, ChronoUnit.MILLIS, ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.DAYS)) {
			tester.accept(unit.getDuration());
		}
	}

	@Test
	public void testInstant() {
		Consumer<Instant> tester = this.factory.createTester();
		tester.accept(Instant.MAX);
		tester.accept(Instant.MIN);
		tester.accept(Instant.now());
		tester.accept(Instant.ofEpochMilli(System.currentTimeMillis()));
	}

	@Test
	public void testLocalDate() {
		Consumer<LocalDate> tester = this.factory.createTester();
		tester.accept(LocalDate.MAX);
		tester.accept(LocalDate.MIN);
		tester.accept(LocalDate.now());
		tester.accept(LocalDate.ofEpochDay(0));
	}

	@Test
	public void testLocalDateTime() {
		Consumer<LocalDateTime> tester = this.factory.createTester();
		tester.accept(LocalDateTime.MAX);
		tester.accept(LocalDateTime.MIN);
		tester.accept(LocalDateTime.now());
		tester.accept(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59)));
		tester.accept(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59)));
		tester.accept(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0)));
		tester.accept(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT));
	}

	@Test
	public void testLocalTime() {
		Consumer<LocalTime> tester = this.factory.createTester();
		tester.accept(LocalTime.MAX);
		tester.accept(LocalTime.MIN);
		tester.accept(LocalTime.now());
		tester.accept(LocalTime.of(23, 59, 59));
		tester.accept(LocalTime.of(23, 59));
		tester.accept(LocalTime.of(23, 0));
	}

	@Test
	public void testMonth() {
		this.factory.createTester(Month.class).run();
	}

	@Test
	public void testMonthDay() {
		Consumer<MonthDay> tester = this.factory.createTester();
		tester.accept(MonthDay.now());
	}

	@Test
	public void testOffsetDateTime() {
		Consumer<OffsetDateTime> tester = this.factory.createTester();
		tester.accept(OffsetDateTime.MAX);
		tester.accept(OffsetDateTime.MIN);
		tester.accept(OffsetDateTime.now(ZoneOffset.UTC));
		tester.accept(OffsetDateTime.now(ZoneOffset.MIN));
		tester.accept(OffsetDateTime.now(ZoneOffset.MAX));
	}

	@Test
	public void testOffsetTime() {
		Consumer<OffsetTime> tester = this.factory.createTester();
		tester.accept(OffsetTime.MAX);
		tester.accept(OffsetTime.MIN);
		tester.accept(OffsetTime.now(ZoneOffset.UTC));
		tester.accept(OffsetTime.now(ZoneOffset.MIN));
		tester.accept(OffsetTime.now(ZoneOffset.MAX));
	}

	@Test
	public void testZonedDateTime() {
		Consumer<ZonedDateTime> tester = this.factory.createTester();
		tester.accept(ZonedDateTime.now(ZoneOffset.UTC));
		tester.accept(ZonedDateTime.now(ZoneOffset.MIN));
		tester.accept(ZonedDateTime.now(ZoneOffset.MAX));
		tester.accept(ZonedDateTime.now(ZoneId.of("America/New_York")));
	}

	@Test
	public void testPeriod() {
		Consumer<Period> tester = this.factory.createTester();
		tester.accept(Period.between(LocalDate.ofEpochDay(0), LocalDate.now()));
		tester.accept(Period.ofMonths(100));
		tester.accept(Period.ofYears(100));
		tester.accept(Period.ZERO);
	}

	@Test
	public void testYear() {
		Consumer<Year> tester = this.factory.createTester();
		tester.accept(Year.of(Year.MAX_VALUE));
		tester.accept(Year.of(Year.MIN_VALUE));
		tester.accept(Year.now());
		tester.accept(Year.of(Instant.EPOCH.atOffset(ZoneOffset.UTC).getYear()));
	}

	@Test
	public void testYearMonth() {
		Consumer<YearMonth> tester = this.factory.createTester();
		tester.accept(YearMonth.of(Year.MAX_VALUE, Month.DECEMBER));
		tester.accept(YearMonth.of(Year.MIN_VALUE, Month.JANUARY));
		tester.accept(YearMonth.now());
		tester.accept(YearMonth.of(Instant.EPOCH.atOffset(ZoneOffset.UTC).getYear(), Instant.EPOCH.atOffset(ZoneOffset.UTC).getMonth()));
	}

	@Test
	public void testZoneId() {
		Consumer<ZoneId> tester = this.factory.createTester();
		tester.accept(ZoneId.of("America/New_York"));
	}

	@Test
	public void testZoneOffset() {
		Consumer<ZoneOffset> tester = this.factory.createTester();
		tester.accept(ZoneOffset.MIN);
		tester.accept(ZoneOffset.MAX);
		tester.accept(ZoneOffset.of("-10")); // Hawaii Standard Time
		tester.accept(ZoneOffset.of("+12:45")); // New Zealand's Chatham Islands
		Random random = new Random(System.currentTimeMillis());
		tester.accept(ZoneOffset.ofHoursMinutesSeconds(random.nextInt(18), random.nextInt(60), random.nextInt(60)));
		tester.accept(ZoneOffset.ofHoursMinutesSeconds(0 - random.nextInt(18), 0 - random.nextInt(60), 0 - random.nextInt(60)));
		tester.accept(ZoneOffset.UTC);
	}
}
