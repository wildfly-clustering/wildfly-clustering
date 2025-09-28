/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jboss.marshalling.ClassTable;
import org.wildfly.clustering.marshalling.ByteBufferMarshalledKey;
import org.wildfly.clustering.marshalling.ByteBufferMarshalledValue;

/**
 * Provides class tables for serializable JDK classes
 * @author Paul Ferraro
 */
enum DefaultClassTableProvider implements Supplier<ClassTable> {
	/** Marshallable objects of {@link java.io}. */
	IO(List.of(
			java.io.Externalizable.class,
			java.io.Serializable.class)),
	/** Marshallable objects of {@link java.lang}. */
	LANG(List.of(
			Class.class,
			StackTraceElement.class)),
	/** Marshallable objects of {@link java.math}. */
	MATH(List.of(
			java.math.BigDecimal.class,
			java.math.BigInteger.class,
			java.math.MathContext.class,
			java.math.RoundingMode.class)),
	/** Marshallable objects of {@link java.net}. */
	NET(List.of(
			java.net.InetAddress.getLoopbackAddress().getClass(),
			java.net.InetSocketAddress.class,
			java.net.URI.class,
			java.net.URL.class)),
	/** Marshallable objects of {@link java.sql}. */
	SQL(List.of(
			java.sql.Date.class,
			java.sql.Time.class,
			java.sql.Timestamp.class)),
	/** Marshallable objects of {@link java.time}. */
	TIME(List.of(
			java.time.Clock.systemDefaultZone().getClass(),
			java.time.DayOfWeek.class,
			java.time.Duration.class,
			java.time.Instant.class,
			java.time.LocalDate.class,
			java.time.LocalDateTime.class,
			java.time.LocalTime.class,
			java.time.Month.class,
			java.time.MonthDay.class,
			java.time.OffsetDateTime.class,
			java.time.OffsetTime.class,
			java.time.Period.class,
			java.time.Year.class,
			java.time.YearMonth.class,
			// ZoneRegion
			java.time.ZoneId.systemDefault().getClass(),
			java.time.ZoneOffset.class,
			java.time.ZonedDateTime.class)),
	/** Marshallable objects of {@link java.util.concurrent.atomic}. */
	CONCURRENT_ATOMIC(List.of(
			java.util.concurrent.atomic.AtomicBoolean.class,
			java.util.concurrent.atomic.AtomicInteger.class,
			java.util.concurrent.atomic.AtomicLong.class,
			java.util.concurrent.atomic.AtomicReference.class)),
	/** Marshallable objects of {@link java.util.concurrent}. */
	CONCURRENT(List.of(
			java.util.concurrent.ArrayBlockingQueue.class,
			java.util.concurrent.ConcurrentHashMap.class,
			java.util.concurrent.ConcurrentHashMap.newKeySet().getClass(),
			java.util.concurrent.ConcurrentLinkedDeque.class,
			java.util.concurrent.ConcurrentLinkedQueue.class,
			java.util.concurrent.ConcurrentSkipListMap.class,
			java.util.concurrent.ConcurrentSkipListSet.class,
			java.util.concurrent.CopyOnWriteArrayList.class,
			java.util.concurrent.CopyOnWriteArraySet.class,
			java.util.concurrent.LinkedBlockingDeque.class,
			java.util.concurrent.LinkedBlockingQueue.class,
			java.util.concurrent.LinkedTransferQueue.class,
			java.util.concurrent.PriorityBlockingQueue.class,
			java.util.concurrent.SynchronousQueue.class,
			java.util.concurrent.TimeUnit.class)),
	/** Marshallable objects of {@link java.util}. */
	UTIL(List.of(
			Collections.checkedCollection(List.of(), Void.class).getClass(),
			// Random access
			Collections.checkedList(List.of(), Void.class).getClass(),
			// Non-random access
			Collections.checkedList(new java.util.LinkedList<>(), Void.class).getClass(),
			Collections.checkedMap(Map.of(), Void.class, Void.class).getClass(),
			Collections.checkedNavigableMap(Collections.emptyNavigableMap(), Void.class, Void.class).getClass(),
			Collections.checkedNavigableSet(Collections.emptyNavigableSet(), Void.class).getClass(),
			Collections.checkedQueue(new java.util.LinkedList<>(), Void.class).getClass(),
			Collections.checkedSet(Set.of(), Void.class).getClass(),
			Collections.checkedSortedMap(Collections.emptySortedMap(), Void.class, Void.class).getClass(),
			Collections.checkedSortedSet(Collections.emptySortedSet(), Void.class).getClass(),
			Collections.singleton(null).getClass(),
			Collections.singletonList(null).getClass(),
			Collections.singletonMap(null, null).getClass(),
			Collections.synchronizedCollection(List.of()).getClass(),
			// Random access
			Collections.synchronizedList(List.of()).getClass(),
			// Sequential access
			Collections.synchronizedList(new java.util.LinkedList<>()).getClass(),
			Collections.synchronizedMap(Map.of()).getClass(),
			Collections.synchronizedNavigableMap(Collections.emptyNavigableMap()).getClass(),
			Collections.synchronizedNavigableSet(Collections.emptyNavigableSet()).getClass(),
			Collections.synchronizedSet(Set.of()).getClass(),
			Collections.synchronizedSortedMap(Collections.emptySortedMap()).getClass(),
			Collections.synchronizedSortedSet(Collections.emptySortedSet()).getClass(),
			Collections.unmodifiableCollection(List.of()).getClass(),
			// Random access
			Collections.unmodifiableList(List.of()).getClass(),
			// Sequential access
			Collections.unmodifiableList(new java.util.LinkedList<>()).getClass(),
			Collections.unmodifiableMap(Map.of()).getClass(),
			Collections.unmodifiableNavigableMap(Collections.emptyNavigableMap()).getClass(),
			Collections.unmodifiableNavigableSet(Collections.emptyNavigableSet()).getClass(),
			Collections.unmodifiableSet(Set.of()).getClass(),
			Collections.unmodifiableSortedMap(Collections.emptySortedMap()).getClass(),
			Collections.unmodifiableSortedSet(Collections.emptySortedSet()).getClass(),
			Collections.newSetFromMap(Map.of()).getClass(),
			java.util.ArrayDeque.class,
			java.util.ArrayList.class,
			java.util.BitSet.class,
			java.util.Currency.class,
			java.util.Date.class,
			java.util.EnumMap.class,
			// RegularEnumSet
			java.util.EnumSet.noneOf(java.util.Locale.IsoCountryCode.class).getClass(),
			// JumboEnumSet
			java.util.EnumSet.noneOf(Character.UnicodeScript.class).getClass(),
			java.util.GregorianCalendar.class,
			java.util.HashMap.class,
			java.util.HashSet.class,
			java.util.Hashtable.class,
			java.util.IdentityHashMap.class,
			java.util.LinkedHashMap.class,
			java.util.LinkedHashSet.class,
			java.util.LinkedList.class,
			// ListN
			java.util.List.of().getClass(),
			// List12
			java.util.List.of(Boolean.TRUE, Boolean.FALSE).getClass(),
			java.util.Locale.class,
			// MapN
			java.util.Map.of().getClass(),
			// Map1
			java.util.Map.of(Boolean.TRUE, Boolean.FALSE).getClass(),
			java.util.PriorityQueue.class,
			java.util.Properties.class,
			// SetN
			java.util.Set.of().getClass(),
			// Set12
			java.util.Set.of(Boolean.TRUE, Boolean.FALSE).getClass(),
			java.util.SimpleTimeZone.class,
			// ZoneInfo
			java.util.TimeZone.getDefault().getClass(),
			java.util.TreeMap.class,
			java.util.TreeSet.class,
			java.util.UUID.class,
			java.util.Vector.class
			)),
		/** Marshallable objects of {@link org.wildfly.clustering.marshalling}. */
		MARSHALLING(List.of(ByteBufferMarshalledKey.class, ByteBufferMarshalledValue.class)),
	;

	private static List<Class<?>> findSerializableClasses(Class<?> targetClass) {
		Class<?>[] childClasses = targetClass.getDeclaredClasses();
		// Include any non-public serializable components/replacements
		return (childClasses.length > 0) ? Stream.concat(Stream.of(targetClass), Stream.of(childClasses).filter(Serializable.class::isAssignableFrom)).toList() : List.of(targetClass);
	}

	private final ClassTable table;

	DefaultClassTableProvider(List<Class<?>> classes) {
		this.table = new IdentityClassTable(classes.stream().map(DefaultClassTableProvider::findSerializableClasses).flatMap(List::stream).toList());
	}

	@Override
	public ClassTable get() {
		return this.table;
	}
}
