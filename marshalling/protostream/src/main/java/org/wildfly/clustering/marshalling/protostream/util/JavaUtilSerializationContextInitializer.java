/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.reflect.DecoratorMarshaller;
import org.wildfly.clustering.marshalling.protostream.reflect.SynchronizedDecoratorMarshaller;

/**
 * @author Paul Ferraro
 */
public class JavaUtilSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public JavaUtilSerializationContextInitializer() {
		super(Collection.class.getPackage());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registerMarshallers(SerializationContext context) {
		ProtoStreamMarshaller<Collection<Object>> linkedListMarshaller = new CollectionMarshaller<>(LinkedList::new);
		ProtoStreamMarshaller<Map<Object, Object>> hashMapMarshaller = new MapMarshaller<>(HashMap::new);
		ProtoStreamMarshaller<AbstractMap.SimpleEntry<Object, Object>> mapEntryMarshaller = new MapEntryMarshaller<>(AbstractMap.SimpleEntry::new);

		context.registerMarshaller(new CollectionMarshaller<>(ArrayDeque::new));
		context.registerMarshaller(new CollectionMarshaller<>(ArrayList::new));
		context.registerMarshaller(Scalar.BYTE_ARRAY.cast(byte[].class).toMarshaller(BitSet.class, BitSet::isEmpty, BitSet::toByteArray, BitSet::new, BitSet::valueOf));
		context.registerMarshaller(new CalendarMarshaller());
		context.registerMarshaller(Scalar.STRING.cast(String.class).toMarshaller(Currency.class, Currency::getCurrencyCode, Currency::getInstance));
		context.registerMarshaller(context.getMarshaller(Instant.class).wrap(Date.class, Date::toInstant, Date::from));
		context.registerMarshaller(new EnumMapMarshaller<>());
		context.registerMarshaller(new EnumSetMarshaller<>());
		context.registerMarshaller(hashMapMarshaller);
		context.registerMarshaller(new CollectionMarshaller<>(HashSet::new));
		context.registerMarshaller(new LinkedHashMapMarshaller<>());
		context.registerMarshaller(new CollectionMarshaller<>(LinkedHashSet::new));
		context.registerMarshaller(linkedListMarshaller);
		context.registerMarshaller(new LocaleMarshaller());
		context.registerMarshaller(new PropertiesMarshaller());
		context.registerMarshaller(Scalar.STRING.cast(String.class).toMarshaller(TimeZone.class, TimeZone::getID, Supplier.of(TimeZone.getTimeZone(ZoneOffset.UTC)), TimeZone::getTimeZone));
		context.registerMarshaller(new SortedMapMarshaller<>(TreeMap::new));
		context.registerMarshaller(new SortedSetMarshaller<>(TreeSet::new));
		context.registerMarshaller(UUIDMarshaller.INSTANCE.asMarshaller());

		context.registerMarshaller(mapEntryMarshaller);
		context.registerMarshaller(new MapEntryMarshaller<>(AbstractMap.SimpleImmutableEntry::new));

		// Empty collections
		context.registerMarshaller(ProtoStreamMarshaller.of(Collections.emptyList()));
		context.registerMarshaller(ProtoStreamMarshaller.of(Collections.emptyMap()));
		context.registerMarshaller(ProtoStreamMarshaller.of(Collections.emptyNavigableMap()));
		context.registerMarshaller(ProtoStreamMarshaller.of(Collections.emptyNavigableSet()));
		context.registerMarshaller(ProtoStreamMarshaller.of(Collections.emptySet()));
		context.registerMarshaller(ProtoStreamMarshaller.of(Collections.emptySortedMap()));
		context.registerMarshaller(ProtoStreamMarshaller.of(Collections.emptySortedSet()));

		// Singleton collections
		context.registerMarshaller(Scalar.ANY.toMarshaller(Collections.singletonList(null).getClass().asSubclass(List.class), list -> list.get(0), Collections::singletonList));
		context.registerMarshaller(mapEntryMarshaller.wrap((Class<Map<Object, Object>>) Collections.singletonMap(null, null).getClass().asSubclass(Map.class), map -> new AbstractMap.SimpleEntry<>(map.entrySet().iterator().next()), entry -> Collections.singletonMap(entry.getKey(), entry.getValue())));
		context.registerMarshaller(Scalar.ANY.toMarshaller(Collections.singleton(null).getClass().asSubclass(Set.class), set -> set.iterator().next(), Collections::singleton));

		// Synchronized collection wrappers
		context.registerMarshaller(new SynchronizedDecoratorMarshaller<>(Collection.class, Collections::synchronizedCollection, Collections.emptyList()));
		context.registerMarshaller(new SynchronizedDecoratorMarshaller<>(List.class, Collections::synchronizedList, new LinkedList<>()));
		context.registerMarshaller(new SynchronizedDecoratorMarshaller<>(Map.class, Collections::synchronizedMap, Collections.emptyMap()));
		context.registerMarshaller(new SynchronizedDecoratorMarshaller<>(NavigableMap.class, Collections::synchronizedNavigableMap, Collections.emptyNavigableMap()));
		context.registerMarshaller(new SynchronizedDecoratorMarshaller<>(NavigableSet.class, Collections::synchronizedNavigableSet, Collections.emptyNavigableSet()));
		context.registerMarshaller(new SynchronizedDecoratorMarshaller<>(List.class, Collections::synchronizedList, Collections.emptyList()));
		context.registerMarshaller(new SynchronizedDecoratorMarshaller<>(Set.class, Collections::synchronizedSet, Collections.emptySet()));
		context.registerMarshaller(new SynchronizedDecoratorMarshaller<>(SortedMap.class, Collections::synchronizedSortedMap, Collections.emptySortedMap()));
		context.registerMarshaller(new SynchronizedDecoratorMarshaller<>(SortedSet.class, Collections::synchronizedSortedSet, Collections.emptySortedSet()));

		// Unmodifiable collection wrappers
		context.registerMarshaller(new DecoratorMarshaller<>(Collection.class, Collections::unmodifiableCollection, Collections.emptyList()));
		context.registerMarshaller(new DecoratorMarshaller<>(List.class, Collections::unmodifiableList, new LinkedList<>()));
		context.registerMarshaller(new DecoratorMarshaller<>(Map.class, Collections::unmodifiableMap, Collections.emptyMap()));
		context.registerMarshaller(new DecoratorMarshaller<>(NavigableMap.class, Collections::unmodifiableNavigableMap, Collections.emptyNavigableMap()));
		context.registerMarshaller(new DecoratorMarshaller<>(NavigableSet.class, Collections::unmodifiableNavigableSet, Collections.emptyNavigableSet()));
		context.registerMarshaller(new DecoratorMarshaller<>(List.class, Collections::unmodifiableList, Collections.emptyList()));
		context.registerMarshaller(new DecoratorMarshaller<>(Set.class, Collections::unmodifiableSet, Collections.emptySet()));
		context.registerMarshaller(new DecoratorMarshaller<>(SortedMap.class, Collections::unmodifiableSortedMap, Collections.emptySortedMap()));
		context.registerMarshaller(new DecoratorMarshaller<>(SortedSet.class, Collections::unmodifiableSortedSet, Collections.emptySortedSet()));

		// Unmodifiable collections
		context.registerMarshaller(unmodifiableCollectionMarshaller(linkedListMarshaller, List.of(Boolean.TRUE).getClass().asSubclass(List.class), List::of));
		context.registerMarshaller(unmodifiableCollectionMarshaller(linkedListMarshaller, List.of().getClass().asSubclass(List.class), List::of));
		context.registerMarshaller(unmodifiableMapMarshaller(hashMapMarshaller, Map.of(Boolean.TRUE, Boolean.FALSE).getClass().asSubclass(Map.class), Map::ofEntries));
		context.registerMarshaller(unmodifiableMapMarshaller(hashMapMarshaller, Map.of().getClass().asSubclass(Map.class), Map::ofEntries));
		context.registerMarshaller(unmodifiableCollectionMarshaller(linkedListMarshaller, Set.of(Boolean.TRUE).getClass().asSubclass(Set.class), Set::of));
		context.registerMarshaller(unmodifiableCollectionMarshaller(linkedListMarshaller, Set.of().getClass().asSubclass(Set.class), Set::of));
	}

	private static <T extends Collection<Object>> ProtoStreamMarshaller<T> unmodifiableCollectionMarshaller(ProtoStreamMarshaller<Collection<Object>> collectionMarshaller, Class<T> targetClass, Function<Object[], T> factory) {
		return collectionMarshaller.wrap(targetClass, collection -> factory.apply(collection.toArray()));
	}

	private static <T extends Map<Object, Object>> ProtoStreamMarshaller<T> unmodifiableMapMarshaller(ProtoStreamMarshaller<Map<Object, Object>> mapMarshaller, Class<T> targetClass, Function<Map.Entry<? extends Object, ? extends Object>[], T> factory) {
		@SuppressWarnings("unchecked")
		Map.Entry<Object, Object>[] entries = new Map.Entry[0];
		return mapMarshaller.wrap(targetClass, map -> factory.apply(map.entrySet().toArray(entries)));
	}
}
