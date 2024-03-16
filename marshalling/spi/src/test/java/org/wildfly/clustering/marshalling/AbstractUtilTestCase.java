/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.test.TestComparator;

/**
 * Generic tests for java.util.* classes.
 * @author Paul Ferraro
 */
public abstract class AbstractUtilTestCase {
	private static final Map<Object, Object> BASIS = Stream.of(1, 2, 3, 4, 5).collect(Collectors.toMap(i -> i, i -> Integer.toString(-i)));

	private final MarshallingTesterFactory factory;

	public AbstractUtilTestCase(MarshallingTesterFactory factory) {
		this.factory = factory;
	}

	@Test
	public void testArrayDeque() {
		Consumer<ArrayDeque<Object>> tester = this.factory.createOrderedCollectionTester();
		tester.accept(new ArrayDeque<>(BASIS.keySet()));
	}

	@Test
	public void testArrayList() {
		Consumer<ArrayList<Object>> tester = this.factory.createOrderedCollectionTester();
		tester.accept(new ArrayList<>(BASIS.keySet()));
	}

	@Test
	public void testBitSet() {
		Consumer<BitSet> tester = this.factory.createTester();

		tester.accept(new BitSet(0));
		BitSet set = new BitSet(7);
		set.set(1);
		set.set(3);
		set.set(5);
		tester.accept(set);
	}

	@Test
	public void testCalendar() {
		Consumer<Calendar> tester = this.factory.createTester();
		LocalDateTime time = LocalDateTime.now();
		// Validate default calendar w/date only
		tester.accept(new Calendar.Builder().setDate(time.getYear(), time.getMonthValue(), time.getDayOfMonth()).build());
		// Validate Gregorian calendar w/locale and date + time
		tester.accept(new Calendar.Builder().setLenient(false).setLocale(Locale.FRANCE).setDate(time.getYear(), time.getMonthValue() - 1, time.getDayOfMonth()).setTimeOfDay(time.getHour(), time.getMinute(), time.getSecond()).build());
		// Validate Japanese Imperial calendar w/full date/time
		tester.accept(new Calendar.Builder().setLocale(Locale.JAPAN).setTimeZone(TimeZone.getTimeZone("Asia/Tokyo")).setInstant(Date.from(time.toInstant(ZoneOffset.UTC))).build());
		// Validate Buddhist calendar
		tester.accept(new Calendar.Builder().setLocale(Locale.forLanguageTag("th_TH")).setTimeZone(TimeZone.getTimeZone("Asia/Bangkok")).build());
	}

	@Test
	public void testCurrency() {
		Consumer<Currency> tester = this.factory.createTester();
		for (Currency currency : Currency.getAvailableCurrencies()) {
			tester.accept(currency);
		}
	}

	@Test
	public void testDate() {
		Consumer<Date> tester = this.factory.createTester();
		tester.accept(Date.from(Instant.EPOCH));
		tester.accept(Date.from(Instant.now()));
	}

	@Test
	public void testEnumMap() {
		Consumer<EnumMap<Thread.State, String>> tester = this.factory.createMapTester();
		EnumMap<Thread.State, String> map = new EnumMap<>(Thread.State.class);
		tester.accept(map);
		for (Thread.State state : EnumSet.allOf(Thread.State.class)) {
			map.put(state, ((state.ordinal() % 2) == 0) ? state.name() : null);
			tester.accept(map);
		}
	}

	@Test
	public void testEnumSet() {
		Consumer<EnumSet<Thread.State>> tester = this.factory.createCollectionTester();
		EnumSet<Thread.State> set = EnumSet.noneOf(Thread.State.class);
		tester.accept(set);
		for (Thread.State state : EnumSet.allOf(Thread.State.class)) {
			set.add(state);
			tester.accept(set);
		}
	}

	@Test
	public void testJumboEnumSet() {
		Consumer<EnumSet<Character.UnicodeScript>> tester = this.factory.createCollectionTester();
		tester.accept(EnumSet.noneOf(Character.UnicodeScript.class));
		tester.accept(EnumSet.of(Character.UnicodeScript.UNKNOWN));
		tester.accept(EnumSet.of(Character.UnicodeScript.ARABIC, Character.UnicodeScript.ARMENIAN, Character.UnicodeScript.AVESTAN, Character.UnicodeScript.BALINESE, Character.UnicodeScript.BAMUM, Character.UnicodeScript.BATAK, Character.UnicodeScript.BENGALI, Character.UnicodeScript.BOPOMOFO, Character.UnicodeScript.BRAHMI, Character.UnicodeScript.BRAILLE, Character.UnicodeScript.BUGINESE, Character.UnicodeScript.BUHID, Character.UnicodeScript.CANADIAN_ABORIGINAL, Character.UnicodeScript.CARIAN));
		tester.accept(EnumSet.complementOf(EnumSet.of(Character.UnicodeScript.UNKNOWN)));
		tester.accept(EnumSet.allOf(Character.UnicodeScript.class));
	}

	@Test
	public void testHashMap() {
		Consumer<HashMap<Object, Object>> tester = this.factory.createMapTester();
		tester.accept(new HashMap<>(BASIS));
	}

	@Test
	public void testHashSet() {
		Consumer<HashSet<Object>> tester = this.factory.createCollectionTester();
		tester.accept(new HashSet<>(BASIS.keySet()));
	}

	@Test
	public void testLinkedHashMap() {
		Consumer<LinkedHashMap<Object, Object>> tester = this.factory.createOrderedMapTester();
		tester.accept(new LinkedHashMap<>(BASIS));
		LinkedHashMap<Object, Object> accessOrderMap = new LinkedHashMap<>(5, 1, true);
		accessOrderMap.putAll(BASIS);
		tester.accept(new LinkedHashMap<>(accessOrderMap));
	}

	@Test
	public void testLinkedHashSet() {
		Consumer<LinkedHashSet<Object>> tester = this.factory.createOrderedCollectionTester();
		tester.accept(new LinkedHashSet<>(BASIS.keySet()));
	}

	@Test
	public void testLinkedList() {
		Consumer<LinkedList<Object>> tester = this.factory.createOrderedCollectionTester();
		tester.accept(new LinkedList<>(BASIS.keySet()));
	}

	@Test
	public void testLocale() {
		Consumer<Locale> tester = this.factory.createTester();
		for (Locale locale : Locale.getAvailableLocales()) {
			tester.accept(locale);
		}
	}

	@Test
	public void testSimpleEntry() {
		Consumer<AbstractMap.SimpleEntry<Object, Object>> tester = this.factory.createTester();
		String key = "key";
		String value = "value";
		tester.accept(new AbstractMap.SimpleEntry<>(null, null));
		tester.accept(new AbstractMap.SimpleEntry<>(key, null));
		tester.accept(new AbstractMap.SimpleEntry<>(key, value));
		tester.accept(new AbstractMap.SimpleEntry<>(value, value));
	}

	@Test
	public void testSimpleImmutableEntry() {
		Consumer<AbstractMap.SimpleImmutableEntry<Object, Object>> tester = this.factory.createTester();
		String key = "key";
		String value = "value";
		tester.accept(new AbstractMap.SimpleImmutableEntry<>(null, null));
		tester.accept(new AbstractMap.SimpleImmutableEntry<>(key, null));
		tester.accept(new AbstractMap.SimpleImmutableEntry<>(key, value));
		tester.accept(new AbstractMap.SimpleImmutableEntry<>(value, value));
	}

	@Test
	public void testTimeZone() {
		Consumer<TimeZone> tester = this.factory.createTester();
		tester.accept(TimeZone.getDefault());
		tester.accept(TimeZone.getTimeZone("GMT"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testTreeMap() {
		Consumer<TreeMap<Object, Object>> tester = this.factory.createOrderedMapTester();

		TreeMap<Object, Object> map = new TreeMap<>();
		map.putAll(BASIS);
		tester.accept(map);

		map = new TreeMap<>((Comparator<Object>) (Comparator<?>) Comparator.reverseOrder());
		map.putAll(BASIS);
		tester.accept(map);

		map = new TreeMap<>(new TestComparator<>());
		map.putAll(BASIS);
		tester.accept(map);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testTreeSet() {
		Consumer<TreeSet<Object>> tester = this.factory.createOrderedCollectionTester();

		TreeSet<Object> set = new TreeSet<>();
		set.addAll(BASIS.keySet());
		tester.accept(set);

		set = new TreeSet<>((Comparator<Object>) (Comparator<?>) Comparator.reverseOrder());
		set.addAll(BASIS.keySet());
		tester.accept(set);

		set = new TreeSet<>(new TestComparator<>());
		set.addAll(BASIS.keySet());
		tester.accept(set);
	}

	@Test
	public void testUUID() {
		Consumer<UUID> tester = this.factory.createTester();
		tester.accept(UUID.randomUUID());
	}

	// java.util.Collections.emptyXXX() methods

	@Test
	public void testEmptyList() {
		Consumer<List<Object>> tester = this.factory.createIdentityTester();
		tester.accept(Collections.emptyList());
	}

	@Test
	public void testEmptyMap() {
		Consumer<Map<Object, Object>> tester = this.factory.createIdentityTester();
		tester.accept(Collections.emptyMap());
	}

	@Test
	public void testEmptyNavigableMap() {
		Consumer<NavigableMap<Object, Object>> tester = this.factory.createIdentityTester();
		tester.accept(Collections.emptyNavigableMap());
	}

	@Test
	public void testEmptyNavigableSet() {
		Consumer<NavigableSet<Object>> tester = this.factory.createIdentityTester();
		tester.accept(Collections.emptyNavigableSet());
	}

	@Test
	public void testEmptySet() {
		Consumer<Set<Object>> tester = this.factory.createIdentityTester();
		tester.accept(Collections.emptySet());
	}

	@Test
	public void testEmptySortedMap() {
		Consumer<SortedMap<Object, Object>> tester = this.factory.createIdentityTester();
		tester.accept(Collections.emptySortedMap());
	}

	@Test
	public void testEmptySortedSet() {
		Consumer<SortedSet<Object>> tester = this.factory.createIdentityTester();
		tester.accept(Collections.emptySortedSet());
	}

	// java.util.Collections.synchronizedXXX(...) methods
	@Test
	public void testSynchronizedCollection() {
		Consumer<Collection<Object>> tester = this.factory.createCollectionTester();
		tester.accept(Collections.synchronizedCollection(new LinkedList<>(BASIS.keySet())));
	}

	@Test
	public void testSynchronizedList() {
		Consumer<List<Object>> tester = this.factory.createOrderedCollectionTester();
		tester.accept(Collections.synchronizedList(new LinkedList<>(BASIS.keySet())));
	}

	@Test
	public void testSynchronizedMap() {
		Consumer<Map<Object, Object>> tester = this.factory.createMapTester();
		tester.accept(Collections.synchronizedMap(new HashMap<>(BASIS)));
	}

	@Test
	public void testSynchronizedNavigableMap() {
		Consumer<Map<Object, Object>> tester = this.factory.createMapTester();
		TreeMap<Object, Object> map = new TreeMap<>();
		map.putAll(BASIS);
		tester.accept(Collections.synchronizedNavigableMap(map));
	}

	@Test
	public void testSynchronizedNavigableSet() {
		Consumer<Set<Object>> tester = this.factory.createOrderedCollectionTester();
		TreeSet<Object> set = new TreeSet<>();
		set.addAll(BASIS.keySet());
		tester.accept(Collections.synchronizedNavigableSet(set));
	}

	@Test
	public void testSynchronizedRandomAccessList() {
		Consumer<List<Object>> tester = this.factory.createOrderedCollectionTester();
		tester.accept(Collections.synchronizedList(new ArrayList<>(BASIS.keySet())));
	}

	@Test
	public void testSynchronizedSet() {
		Consumer<Set<Object>> tester = this.factory.createCollectionTester();
		tester.accept(Collections.synchronizedSet(new HashSet<>(BASIS.keySet())));
	}

	@Test
	public void testSynchronizedSortedMap() {
		Consumer<SortedMap<Object, Object>> tester = this.factory.createTester();
		TreeMap<Object, Object> map = new TreeMap<>();
		map.putAll(BASIS);
		tester.accept(Collections.synchronizedSortedMap(map));
	}

	@Test
	public void testSynchronizedSortedSet() {
		Consumer<Set<Object>> tester = this.factory.createOrderedCollectionTester();
		TreeSet<Object> set = new TreeSet<>();
		set.addAll(BASIS.keySet());
		tester.accept(Collections.synchronizedSortedSet(set));
	}

	// java.util.Collections.singletonXXX(...) methods
	@Test
	public void testSingletonList() {
		Consumer<List<Object>> tester = this.factory.createOrderedCollectionTester();
		tester.accept(Collections.singletonList(null));
		tester.accept(Collections.singletonList("foo"));
	}

	@Test
	public void testSingletonMap() {
		Consumer<Map<Object, Object>> tester = this.factory.createMapTester();
		tester.accept(Collections.singletonMap(null, null));
		tester.accept(Collections.singletonMap("foo", null));
		tester.accept(Collections.singletonMap("foo", "bar"));
	}

	@Test
	public void testSingletonSet() {
		Consumer<Set<Object>> tester = this.factory.createCollectionTester();
		tester.accept(Collections.singleton(null));
		tester.accept(Collections.singleton("foo"));
	}

	// java.util.Collections.unmodifiableXXX(...) methods
	@Test
	public void testUnmodifiableCollection() {
		Consumer<Collection<Object>> tester = this.factory.createCollectionTester();
		tester.accept(Collections.unmodifiableCollection(new LinkedList<>(BASIS.keySet())));
	}

	@Test
	public void testUnmodifiableList() {
		Consumer<List<Object>> tester = this.factory.createOrderedCollectionTester();
		tester.accept(Collections.unmodifiableList(new LinkedList<>(BASIS.keySet())));

		tester.accept(List.of());
		tester.accept(List.of(0));
		tester.accept(List.of(0, 1));
		tester.accept(List.of(0, 1, 2, 3));
		tester.accept(List.of(0, 1, 2, 3, 4));
		tester.accept(List.of(0, 1, 2, 3, 4, 5));
		tester.accept(List.of(0, 1, 2, 3, 4, 5, 6));
		tester.accept(List.of(0, 1, 2, 3, 4, 5, 6, 7));
		tester.accept(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8));
		tester.accept(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		tester.accept(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void testUnmodifiableMap() {
		Consumer<Map<Object, Object>> tester = this.factory.createMapTester();
		tester.accept(Collections.unmodifiableMap(new HashMap<>(BASIS)));

		tester.accept(Map.of());
		tester.accept(Map.of(0, "0"));
		tester.accept(Map.of(0, "0", 1, "1"));
		tester.accept(Map.of(0, "0", 1, "1", 2, "2"));
		tester.accept(Map.of(0, "0", 1, "1", 2, "2", 3, "3"));
		tester.accept(Map.of(0, "0", 1, "1", 2, "2", 3, "3", 4, "4"));
		tester.accept(Map.of(0, "0", 1, "1", 2, "2", 3, "3", 4, "4", 5, "5"));
		tester.accept(Map.of(0, "0", 1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6"));
		tester.accept(Map.of(0, "0", 1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6", 7, "7"));
		tester.accept(Map.of(0, "0", 1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6", 7, "7", 8, "8"));
		tester.accept(Map.of(0, "0", 1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6", 7, "7", 8, "8", 9, "9"));
		tester.accept(Map.ofEntries(Map.entry(0, "1")));
		tester.accept(Map.ofEntries(Map.entry(0, "0"), Map.entry(1, "1")));
		tester.accept(Map.ofEntries(Map.entry(0, "0"), Map.entry(1, "1"), Map.entry(2, "2")));
	}

	@Test
	public void testUnmodifiableNavigableMap() {
		Consumer<NavigableMap<Object, Object>> tester = this.factory.createOrderedMapTester();
		TreeMap<Object, Object> map = new TreeMap<>();
		map.putAll(BASIS);
		tester.accept(Collections.unmodifiableNavigableMap(map));
	}

	@Test
	public void testUnmodifiableNavigableSet() {
		Consumer<NavigableSet<Object>> tester = this.factory.createOrderedCollectionTester();
		TreeSet<Object> set = new TreeSet<>();
		set.addAll(BASIS.keySet());
		tester.accept(Collections.unmodifiableNavigableSet(set));
	}

	@Test
	public void testUnmodifiableSet() {
		Consumer<Set<Object>> tester = this.factory.createCollectionTester();
		tester.accept(Collections.unmodifiableSet(new HashSet<>(BASIS.keySet())));

		tester.accept(Set.of());
		tester.accept(Set.of(0));
		tester.accept(Set.of(0, 1));
		tester.accept(Set.of(0, 1, 2, 3));
		tester.accept(Set.of(0, 1, 2, 3, 4));
		tester.accept(Set.of(0, 1, 2, 3, 4, 5));
		tester.accept(Set.of(0, 1, 2, 3, 4, 5, 6));
		tester.accept(Set.of(0, 1, 2, 3, 4, 5, 6, 7));
		tester.accept(Set.of(0, 1, 2, 3, 4, 5, 6, 7));
		tester.accept(Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8));
		tester.accept(Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		tester.accept(Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void testUnmodifiableSortedMap() {
		Consumer<SortedMap<Object, Object>> tester = this.factory.createOrderedMapTester();
		TreeMap<Object, Object> map = new TreeMap<>();
		map.putAll(BASIS);
		tester.accept(Collections.unmodifiableSortedMap(map));
	}

	@Test
	public void testUnmodifiableSortedSet() {
		Consumer<SortedSet<Object>> tester = this.factory.createOrderedCollectionTester();
		TreeSet<Object> set = new TreeSet<>();
		set.addAll(BASIS.keySet());
		tester.accept(Collections.unmodifiableSortedSet(set));
	}

	static <T extends Collection<?>> void assertCollectionEquals(T expected, T actual) {
		assertSame(expected.getClass(), actual.getClass());
		assertEquals(expected.size(), actual.size());
		assertTrue(expected.containsAll(actual));
	}

	static <T extends Map<?, ?>> void assertMapEquals(T expected, T actual) {
		assertSame(expected.getClass(), actual.getClass());
		assertEquals(expected.size(), actual.size());
		assertTrue(expected.keySet().containsAll(actual.keySet()), actual.keySet()::toString);
		for (Map.Entry<?, ?> entry : expected.entrySet()) {
			assertEquals(entry.getValue(), actual.get(entry.getKey()));
		}
	}

	static <T extends Map<?, ?>> void assertLinkedMapEquals(T expected, T actual) {
		assertSame(expected.getClass(), actual.getClass());
		assertEquals(expected.size(), actual.size());
		// Change access order
		expected.get(expected.keySet().iterator().next());
		actual.get(actual.keySet().iterator().next());
		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<?, ?>> expectedEntries = (Iterator<Map.Entry<?, ?>>) (Iterator<?>) expected.entrySet().iterator();
		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<?, ?>> actualEntries = (Iterator<Map.Entry<?, ?>>) (Iterator<?>) actual.entrySet().iterator();
		while (expectedEntries.hasNext() && actualEntries.hasNext()) {
			Map.Entry<?, ?> expectedEntry = expectedEntries.next();
			Map.Entry<?, ?> actualEntry = actualEntries.next();
			assertEquals(expectedEntry.getKey(), actualEntry.getKey());
			assertEquals(expectedEntry.getValue(), actualEntry.getValue());
		}
	}
}
