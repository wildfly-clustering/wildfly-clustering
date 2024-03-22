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
import org.wildfly.common.function.ExceptionBiConsumer;

/**
 * Generic identity table.
 * @param <T> the table entry type
 * @author Paul Ferraro
 */
public interface IdentityTable<T> {

	ExceptionBiConsumer<Marshaller, T, IOException> findWriter(T value);

	T read(Unmarshaller unmarshaller) throws IOException, ClassNotFoundException;

	static <T> IdentityTable<T> from(List<T> entries) {
		IntSerializer indexSerializer = IndexSerializer.select(entries.size());
		Map<T, Integer> indexes = new IdentityHashMap<>(entries.size());
		ListIterator<T> iterator = entries.listIterator();
		while (iterator.hasNext()) {
			indexes.putIfAbsent(iterator.next(), iterator.previousIndex());
		}
		ExceptionBiConsumer<Marshaller, T, IOException> writer = new ExceptionBiConsumer<>() {
			@Override
			public void accept(Marshaller marshaller, T value) throws IOException {
				int index = indexes.get(value);
				indexSerializer.writeInt(marshaller, index);
			}
		};
		return new IdentityTable<>() {
			@Override
			public ExceptionBiConsumer<Marshaller, T, IOException> findWriter(T value) {
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
