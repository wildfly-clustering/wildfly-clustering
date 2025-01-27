/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;

import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * Abstract marshaller for a {@link Map} that writes the entries of the map.
 * @author Paul Ferraro
 * @param <K> the map key type
 * @param <V> the map value type
 * @param <T> the map type of this marshaller
 */
public abstract class AbstractMapMarshaller<K, V, T extends Map<K, V>> implements ProtoStreamMarshaller<T> {
	protected static final int ENTRY_INDEX = 1;

	private final Class<? extends T> mapClass;

	public AbstractMapMarshaller(Class<? extends T> mapClass) {
		this.mapClass = mapClass;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, T map) throws IOException {
		synchronized (map) { // Avoid ConcurrentModificationException
			for (Map.Entry<K, V> entry : map.entrySet()) {
				writer.writeObject(ENTRY_INDEX, new AbstractMap.SimpleEntry<>(entry));
			}
		}
	}

	@Override
	public Class<? extends T> getJavaClass() {
		return this.mapClass;
	}
}
