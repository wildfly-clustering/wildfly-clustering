/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.function.IntFunction;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;

/**
 * A ProtoStrema marshaller for sized collections.
 * @author Paul Ferraro
 * @param <E> the element type
 * @param <T> the collection type
 */
public class SizedCollectionMarshaller<E, T extends Collection<E>> extends AbstractCollectionMarshaller<E, T> {

	private final IntFunction<T> factory;

	/**
	 * Creates a marshaller for a collection.
	 * @param factory the collection factory
	 */
	@SuppressWarnings("unchecked")
	public SizedCollectionMarshaller(IntFunction<T> factory) {
		super((Class<T>) factory.apply(0).getClass());
		this.factory = factory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T readFrom(ProtoStreamReader reader) throws IOException {
		List<E> elements = new LinkedList<>();
		while (!reader.isAtEnd()) {
			int tag = reader.readTag();
			switch (WireType.getTagFieldNumber(tag)) {
				case ELEMENT_INDEX -> elements.add((E) reader.readAny());
				default -> reader.skipField(tag);
			}
		}
		T result = this.factory.apply(elements.size());
		result.addAll(elements);
		return result;
	}
}
