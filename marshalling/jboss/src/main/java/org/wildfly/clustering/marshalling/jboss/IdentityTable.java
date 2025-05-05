/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.Unmarshaller;
import org.wildfly.clustering.marshalling.IndexSerializer;
import org.wildfly.clustering.marshalling.IntSerializer;

/**
 * Generic identity table.
 * @param <T> the table entry type
 * @author Paul Ferraro
 */
public interface IdentityTable<T> {

	Writable<T> findWriter(T value);

	T read(Unmarshaller unmarshaller) throws IOException, ClassNotFoundException;

	static <T> IdentityTable<T> from(List<T> entries) {
		IntSerializer indexSerializer = IndexSerializer.select(entries.size());
		Map<T, Integer> indexes = new IdentityHashMap<>(entries.size());
		ListIterator<T> iterator = entries.listIterator();
		while (iterator.hasNext()) {
			indexes.putIfAbsent(iterator.next(), iterator.previousIndex());
		}
		Writable<T> writer = new Writable<>() {
			@Override
			public void write(Marshaller marshaller, T value) throws IOException {
				int index = indexes.get(value);
				indexSerializer.writeInt(marshaller, index);
			}
		};
		return new IdentityTable<>() {
			@Override
			public Writable<T> findWriter(T value) {
				return indexes.containsKey(value) ? writer : null;
			}

			@Override
			public T read(Unmarshaller unmarshaller) throws IOException, ClassNotFoundException {
				int index = indexSerializer.readInt(unmarshaller);
				return entries.get(index);
			}
		};
	}
}
