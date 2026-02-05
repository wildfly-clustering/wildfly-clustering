/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.local.scheduler;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

/**
 * {@link ScheduledEntries} implemented using a {@link ConcurrentSkipListSet}, where entries are sorted based on the entry value.
 * Both {@link #add(Object, Comparable)} and {@link #remove(Object)} run in O(log N) time.
 * @author Paul Ferraro
 */
class SortedScheduledEntries<K, V> implements ScheduledEntries<K, V> {
	private final SortedSet<Map.Entry<K, V>> sorted;
	private final Map<K, V> entries = new ConcurrentHashMap<>();

	/**
	 * Creates a new entries object whose iteration order is based on the specified comparator.
	 * @param comparator the comparator used to determine the iteration order
	 */
	SortedScheduledEntries(Comparator<Map.Entry<K, V>> comparator) {
		this.sorted = new ConcurrentSkipListSet<>(comparator);
	}

	static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K, V>> defaultComparator() {
		return new Comparator<>() {
			private final List<ToIntFunction<K>> hashFunctions = List.of(Object::hashCode, System::identityHashCode);

			@Override
			public int compare(Map.Entry<K, V> entry1, Map.Entry<K, V> entry2) {
				K key1 = entry1.getKey();
				K key2 = entry2.getKey();
				// Check for key equality first
				if (key1.equals(key2)) return 0;
				// If keys are not equal, this method cannot return 0
				// Compare by value
				int result = entry1.getValue().compareTo(entry2.getValue());
				if (result != 0) return result;
				// Compare by key hash
				for (ToIntFunction<K> hashFunction : this.hashFunctions) {
					result = Integer.compare(hashFunction.applyAsInt(key1), hashFunction.applyAsInt(key2));
					if (result != 0) return result;
				}
				// Highly unlikely, but use fixed value as a last resort
				return 1;
			}
		};
	}

	@Override
	public boolean isSorted() {
		return true;
	}

	@Override
	public void add(K key, V value) {
		V oldValue = this.entries.put(key, value);
		if (oldValue != null) {
			this.sorted.remove(new Entry<>(key, oldValue));
		}
		this.sorted.add(new Entry<>(key, value));
	}

	@Override
	public void remove(K key) {
		V value = this.entries.remove(key);
		if (value != null) {
			this.sorted.remove(new Entry<>(key, value));
		}
	}

	@Override
	public boolean contains(K key) {
		return this.entries.containsKey(key);
	}

	@Override
	public Map.Entry<K, V> peek() {
		try {
			return this.sorted.first();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Override
	public Stream<Map.Entry<K, V>> stream() {
		return this.sorted.stream();
	}

	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		Iterator<Map.Entry<K, V>> iterator = this.sorted.iterator();
		Map<K, V> entries = this.entries;
		return new Iterator<>() {
			private K current;

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public Map.Entry<K, V> next() {
				Map.Entry<K, V> next = iterator.next();
				this.current = next.getKey();
				return next;
			}

			@Override
			public void remove() {
				iterator.remove();
				entries.remove(this.current);
			}
		};
	}

	@Override
	public void forEach(Consumer<? super Map.Entry<K, V>> action) {
		this.sorted.forEach(action);
	}

	@Override
	public Spliterator<Map.Entry<K, V>> spliterator() {
		return this.sorted.spliterator();
	}

	@Override
	public String toString() {
		return this.sorted.toString();
	}

	/**
	 * A {@link SimpleImmutableEntry} whose equality is based solely on the entry key.
	 */
	private static class Entry<K, V> extends SimpleImmutableEntry<K, V> {
		private static final long serialVersionUID = -1818780078437540182L;

		Entry(K key, V value) {
			super(key, value);
		}

		@Override
		public boolean equals(Object object) {
			if (!(object instanceof Map.Entry entry)) return false;
			return this.getKey().equals(entry.getKey());
		}

		@Override
		public int hashCode() {
			return this.getKey().hashCode();
		}

		@Override
		public String toString() {
			return this.getKey().toString();
		}
	}
}
