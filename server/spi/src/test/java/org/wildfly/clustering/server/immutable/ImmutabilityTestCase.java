/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.immutable;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.security.AllPermission;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DecimalStyle;
import java.time.temporal.ValueRange;
import java.time.temporal.WeekFields;
import java.time.zone.ZoneOffsetTransitionRule;
import java.time.zone.ZoneOffsetTransitionRule.TimeDefinition;
import java.time.zone.ZoneRules;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DefaultImmutability}
 *
 * @author Paul Ferraro
 */
public class ImmutabilityTestCase {

	@Test
	public void test() throws Exception {
		this.test(Immutability.composite(EnumSet.allOf(DefaultImmutability.class)));
	}

	protected void test(Immutability immutability) throws Exception {
		assertThat(immutability.test(new Object())).isFalse();
		assertThat(immutability.test(new Date())).isFalse();
		assertThat(immutability.test(new AtomicInteger())).isFalse();
		assertThat(immutability.test(new AtomicLong())).isFalse();
		assertThat(immutability.test(null)).isTrue();
		assertThat(immutability.test(Collections.emptyEnumeration())).isTrue();
		assertThat(immutability.test(Collections.emptyIterator())).isTrue();
		assertThat(immutability.test(Collections.emptyList())).isTrue();
		assertThat(immutability.test(Collections.emptyListIterator())).isTrue();
		assertThat(immutability.test(Collections.emptyMap())).isTrue();
		assertThat(immutability.test(Collections.emptyNavigableMap())).isTrue();
		assertThat(immutability.test(Collections.emptyNavigableSet())).isTrue();
		assertThat(immutability.test(Collections.emptySet())).isTrue();
		assertThat(immutability.test(Collections.emptySortedMap())).isTrue();
		assertThat(immutability.test(Collections.emptySortedSet())).isTrue();
		assertThat(immutability.test(Boolean.TRUE)).isTrue();
		assertThat(immutability.test('a')).isTrue();
		assertThat(immutability.test(this.getClass())).isTrue();
		assertThat(immutability.test(Currency.getInstance(Locale.US))).isTrue();
		assertThat(immutability.test(Locale.getDefault())).isTrue();
		assertThat(immutability.test(Integer.valueOf(1).byteValue())).isTrue();
		assertThat(immutability.test(Integer.valueOf(1).shortValue())).isTrue();
		assertThat(immutability.test(1)).isTrue();
		assertThat(immutability.test(1L)).isTrue();
		assertThat(immutability.test(1F)).isTrue();
		assertThat(immutability.test(1.0)).isTrue();
		assertThat(immutability.test(BigInteger.valueOf(1))).isTrue();
		assertThat(immutability.test(BigDecimal.valueOf(1))).isTrue();
		assertThat(immutability.test(InetAddress.getLocalHost())).isTrue();
		assertThat(immutability.test(new InetSocketAddress(InetAddress.getLocalHost(), 80))).isTrue();
		assertThat(immutability.test(MathContext.UNLIMITED)).isTrue();
		assertThat(immutability.test("test")).isTrue();
		assertThat(immutability.test(TimeZone.getDefault())).isTrue();
		assertThat(immutability.test(UUID.randomUUID())).isTrue();
		assertThat(immutability.test(TimeUnit.DAYS)).isTrue();
		File file = new File(System.getProperty("user.home"));
		assertThat(immutability.test(file)).isTrue();
		assertThat(immutability.test(file.toURI())).isTrue();
		assertThat(immutability.test(file.toURI().toURL())).isTrue();
		assertThat(immutability.test(FileSystems.getDefault().getRootDirectories().iterator().next())).isTrue();
		assertThat(immutability.test(new AllPermission())).isTrue();

		assertThat(immutability.test(DateTimeFormatter.BASIC_ISO_DATE)).isTrue();
		assertThat(immutability.test(DecimalStyle.STANDARD)).isTrue();
		assertThat(immutability.test(Duration.ZERO)).isTrue();
		assertThat(immutability.test(Instant.now())).isTrue();
		assertThat(immutability.test(LocalDate.now())).isTrue();
		assertThat(immutability.test(LocalDateTime.now())).isTrue();
		assertThat(immutability.test(LocalTime.now())).isTrue();
		assertThat(immutability.test(MonthDay.now())).isTrue();
		assertThat(immutability.test(Period.ZERO)).isTrue();
		assertThat(immutability.test(ValueRange.of(0L, 10L))).isTrue();
		assertThat(immutability.test(WeekFields.ISO)).isTrue();
		assertThat(immutability.test(Year.now())).isTrue();
		assertThat(immutability.test(YearMonth.now())).isTrue();
		assertThat(immutability.test(ZoneOffset.UTC)).isTrue();
		assertThat(immutability.test(ZoneRules.of(ZoneOffset.UTC).nextTransition(Instant.now()))).isTrue();
		assertThat(immutability.test(ZoneOffsetTransitionRule.of(Month.JANUARY, 1, DayOfWeek.SUNDAY, LocalTime.MIDNIGHT, true, TimeDefinition.STANDARD, ZoneOffset.UTC, ZoneOffset.ofHours(1), ZoneOffset.ofHours(2)))).isTrue();
		assertThat(immutability.test(ZoneRules.of(ZoneOffset.UTC))).isTrue();
		assertThat(immutability.test(ZonedDateTime.now())).isTrue();
		assertThat(immutability.test(new JCIPImmutableObject())).isTrue();

		assertThat(immutability.test(Collections.singleton("1"))).isTrue();
		assertThat(immutability.test(Collections.singletonList("1"))).isTrue();
		assertThat(immutability.test(Collections.singletonMap("1", "2"))).isTrue();

		assertThat(immutability.test(Collections.singleton(new JCIPImmutableObject()))).isTrue();
		assertThat(immutability.test(Collections.singletonList(new JCIPImmutableObject()))).isTrue();
		assertThat(immutability.test(Collections.singletonMap("1", new JCIPImmutableObject()))).isTrue();
		assertThat(immutability.test(new AbstractMap.SimpleImmutableEntry<>("1", new JCIPImmutableObject()))).isTrue();

		assertThat(immutability.test(Collections.unmodifiableCollection(Arrays.asList("1", "2")))).isTrue();
		assertThat(immutability.test(Collections.unmodifiableList(Arrays.asList("1", "2")))).isTrue();
		assertThat(immutability.test(Collections.unmodifiableMap(Collections.singletonMap("1", "2")))).isTrue();
		assertThat(immutability.test(Collections.unmodifiableNavigableMap(new TreeMap<>(Collections.singletonMap("1", "2"))))).isTrue();
		assertThat(immutability.test(Collections.unmodifiableNavigableSet(new TreeSet<>(Collections.singleton("1"))))).isTrue();
		assertThat(immutability.test(Collections.unmodifiableSet(Collections.singleton("1")))).isTrue();
		assertThat(immutability.test(Collections.unmodifiableSortedMap(new TreeMap<>(Collections.singletonMap("1", "2"))))).isTrue();
		assertThat(immutability.test(Collections.unmodifiableSortedSet(new TreeSet<>(Collections.singleton("1"))))).isTrue();
		assertThat(immutability.test(List.of())).isTrue();
		assertThat(immutability.test(List.of(1))).isTrue();
		assertThat(immutability.test(List.of(1, 2))).isTrue();
		assertThat(immutability.test(List.of(1, 2, 3))).isTrue();
		assertThat(immutability.test(List.of(1, 2, 3, 4))).isTrue();
		assertThat(immutability.test(List.of(1, 2, 3, 4, 5))).isTrue();
		assertThat(immutability.test(List.of(1, 2, 3, 4, 5, 6))).isTrue();
		assertThat(immutability.test(List.of(1, 2, 3, 4, 5, 6, 7))).isTrue();
		assertThat(immutability.test(List.of(1, 2, 3, 4, 5, 6, 7, 8))).isTrue();
		assertThat(immutability.test(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9))).isTrue();
		assertThat(immutability.test(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))).isTrue();
		assertThat(immutability.test(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11))).isTrue();
		assertThat(immutability.test(Set.of())).isTrue();
		assertThat(immutability.test(Set.of(1))).isTrue();
		assertThat(immutability.test(Set.of(1, 2))).isTrue();
		assertThat(immutability.test(Set.of(1, 2, 3))).isTrue();
		assertThat(immutability.test(Set.of(1, 2, 3, 4))).isTrue();
		assertThat(immutability.test(Set.of(1, 2, 3, 4, 5))).isTrue();
		assertThat(immutability.test(Set.of(1, 2, 3, 4, 5, 6))).isTrue();
		assertThat(immutability.test(Set.of(1, 2, 3, 4, 5, 6, 7))).isTrue();
		assertThat(immutability.test(Set.of(1, 2, 3, 4, 5, 6, 7, 8))).isTrue();
		assertThat(immutability.test(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9))).isTrue();
		assertThat(immutability.test(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))).isTrue();
		assertThat(immutability.test(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11))).isTrue();
		assertThat(immutability.test(Map.of())).isTrue();
		assertThat(immutability.test(Map.of(1, "1"))).isTrue();
		assertThat(immutability.test(Map.of(1, "1", 2, "2"))).isTrue();
		assertThat(immutability.test(Map.of(1, "1", 2, "2", 3, "3"))).isTrue();
		assertThat(immutability.test(Map.of(1, "1", 2, "2", 3, "3", 4, "4"))).isTrue();
		assertThat(immutability.test(Map.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5"))).isTrue();
		assertThat(immutability.test(Map.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6"))).isTrue();
		assertThat(immutability.test(Map.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6", 7, "7"))).isTrue();
		assertThat(immutability.test(Map.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6", 7, "7", 8, "8"))).isTrue();
		assertThat(immutability.test(Map.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6", 7, "7", 8, "8", 9, "9"))).isTrue();
		assertThat(immutability.test(Map.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6", 7, "7", 8, "8", 9, "9", 10, "10"))).isTrue();
		assertThat(immutability.test(Map.ofEntries())).isTrue();
		assertThat(immutability.test(Map.ofEntries(Map.entry(1, "1")))).isTrue();
		assertThat(immutability.test(Map.ofEntries(Map.entry(1, "1"), Map.entry(2, "2")))).isTrue();
		assertThat(immutability.test(new AbstractMap.SimpleImmutableEntry<>(Boolean.TRUE, Boolean.TRUE))).isTrue();

		Object mutableObject = new AtomicInteger();
		assertThat(immutability.test(Collections.singletonList(mutableObject))).isFalse();
		assertThat(immutability.test(Collections.singletonMap("1", mutableObject))).isFalse();
		assertThat(immutability.test(new AbstractMap.SimpleImmutableEntry<>("1", mutableObject))).isFalse();
		assertThat(immutability.test(new AbstractMap.SimpleEntry<>(Boolean.TRUE, Boolean.TRUE))).isFalse();
	}

	@net.jcip.annotations.Immutable
	static class JCIPImmutableObject {
	}
}
