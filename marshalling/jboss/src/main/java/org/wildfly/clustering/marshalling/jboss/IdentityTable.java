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
 * @author Paul Ferraro
 */
public interface IdentityTable<T> {

	ExceptionBiConsumer<Marshaller, T, IOException> findWriter(T object);

	T read(Unmarshaller unmarshaller) throws IOException;

	static <T> IdentityTable<T> from(List<T> objects) {
		IntSerializer indexSerializer = IndexSerializer.select(objects.size());
		Map<Object, Integer> indexes = new IdentityHashMap<>(objects.size());
		ListIterator<T> iterator = objects.listIterator();
		while (iterator.hasNext()) {
			indexes.putIfAbsent(iterator.next(), iterator.previousIndex());
		}
		ExceptionBiConsumer<Marshaller, T, IOException> writer = (marshaller, object) -> indexSerializer.writeInt(marshaller, indexes.get(object));
		return new IdentityTable<>() {
			@Override
			public ExceptionBiConsumer<Marshaller, T, IOException> findWriter(T object) {
				Integer index = indexes.get(object);
				return (index != null) ? writer : null;
			}

			@Override
			public T read(Unmarshaller unmarshaller) throws IOException {
				return objects.get(indexSerializer.readInt(unmarshaller));
			}
		};
	}
}
