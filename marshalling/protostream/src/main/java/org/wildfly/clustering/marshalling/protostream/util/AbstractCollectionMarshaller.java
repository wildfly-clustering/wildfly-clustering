/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.util.Collection;

import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Abstract collection marshaller that writes the elements of the collection.
 * @author Paul Ferraro
 * @param <E> the collection element type
 * @param <T> the collection type of this marshaller
 */
public abstract class AbstractCollectionMarshaller<E, T extends Collection<E>> implements ProtoStreamMarshaller<T> {
	/** Index of the repeated element field */
	protected static final int ELEMENT_INDEX = 1;

	private final Class<? extends T> collectionClass;

	/**
	 * Creates a collection marshaller for the specified implementation class.
	 * @param collectionClass the collection implementation class.
	 */
	protected AbstractCollectionMarshaller(Class<? extends T> collectionClass) {
		this.collectionClass = collectionClass;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, T collection) throws IOException {
		synchronized (collection) { // Avoid ConcurrentModificationException
			for (E element : collection) {
				writer.writeAny(ELEMENT_INDEX, element);
			}
		}
	}

	@Override
	public Class<? extends T> getJavaClass() {
		return this.collectionClass;
	}
}
