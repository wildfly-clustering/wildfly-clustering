/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.immutable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests the immutability of unmodifiable collection and map wrappers.
 * N.B. Strictly speaking, an unmodifiable collection is not necessarily immutable since the collection can still be modified through a reference to the delegate collection.
 * Were this the case, the immutability test would also run against the delegate collection - and fail, forcing replication.
 * @author Paul Ferraro
 */
class UnmodifiableImmutability implements Immutability {

	static <T> Set<T> identitySet(Collection<T> collection) {
		Set<T> set = !collection.isEmpty() ? Collections.newSetFromMap(new IdentityHashMap<>(collection.size())) : Set.of();
		for (T element : collection) {
			set.add(element);
		}
		return set;
	}

	private final Set<Class<?>> unmodifiableCollectionClasses = identitySet(List.of(
			Collections.singleton(null).getClass(),
			Collections.singletonList(null).getClass(),
			Collections.unmodifiableCollection(Collections.emptyList()).getClass(),
			Collections.unmodifiableList(Collections.emptyList()).getClass(),
			Collections.unmodifiableNavigableSet(Collections.emptyNavigableSet()).getClass(),
			Collections.unmodifiableSet(Collections.emptySet()).getClass(),
			Collections.unmodifiableSortedSet(Collections.emptySortedSet()).getClass(),
			List.of().getClass(), // ListN
			List.of(Boolean.TRUE).getClass(), // List12
			Set.of().getClass(), // SetN
			Set.of(Boolean.TRUE).getClass())); // Set12

	private final Set<Class<?>> unmodifiableMapClasses = identitySet(List.of(
			Collections.singletonMap(null, null).getClass(),
			Collections.unmodifiableMap(Collections.emptyMap()).getClass(),
			Collections.unmodifiableNavigableMap(Collections.emptyNavigableMap()).getClass(),
			Collections.unmodifiableSortedMap(Collections.emptySortedMap()).getClass(),
			Map.of().getClass(), // MapN
			Map.of(Boolean.TRUE, Boolean.TRUE).getClass())); // Map1

	private final Set<Class<?>> unmodifiableMapEntryClasses = identitySet(List.of(AbstractMap.SimpleImmutableEntry.class));

	private final Immutability elementImmutability;

	UnmodifiableImmutability(Immutability elementImmutability) {
		this.elementImmutability = elementImmutability;
	}

	@Override
	public boolean test(Object object) {
		if (object == null) return true;
		Class<?> objectClass = object.getClass();
		if (this.unmodifiableCollectionClasses.contains(objectClass)) {
			// An unmodifiable collection is immutable if its members are immutable.
			for (Object element : (Collection<?>) object) {
				if (!this.elementImmutability.test(element)) return false;
			}
			return true;
		}
		if (this.unmodifiableMapClasses.contains(objectClass)) {
			// An unmodifiable map is immutable if its entries are immutable.
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
				if (!this.test(entry)) return false;
			}
			return true;
		}
		if (this.unmodifiableMapEntryClasses.contains(objectClass)) {
			return this.test((Map.Entry<?, ?>) object);
		}
		return false;
	}

	// An unmodifiable map entry is immutable if its key and value are immutable.
	private boolean test(Map.Entry<?, ?> entry) {
		return this.elementImmutability.test(entry.getKey()) && this.elementImmutability.test(entry.getValue());
	}
}
