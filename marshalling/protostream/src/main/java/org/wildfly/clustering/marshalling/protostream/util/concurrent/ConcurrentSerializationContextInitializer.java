/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util.concurrent;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.util.CollectionMarshaller;
import org.wildfly.clustering.marshalling.protostream.util.MapMarshaller;
import org.wildfly.clustering.marshalling.protostream.util.SortedMapMarshaller;
import org.wildfly.clustering.marshalling.protostream.util.SortedSetMarshaller;

/**
 * @author Paul Ferraro
 */
public class ConcurrentSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public ConcurrentSerializationContextInitializer() {
		super("java.util.concurrent.proto");
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		@SuppressWarnings("unchecked")
		ProtoStreamMarshaller<Collection<Object>> collectionMarshaller = (ProtoStreamMarshaller<Collection<Object>>) (ProtoStreamMarshaller<?>) context.getMarshaller(LinkedList.class);

		context.registerMarshaller(new MapMarshaller<>(ConcurrentHashMap::new));
		context.registerMarshaller(new CollectionMarshaller<>(ConcurrentHashMap::newKeySet));
		context.registerMarshaller(new CollectionMarshaller<>(ConcurrentLinkedDeque::new));
		context.registerMarshaller(new CollectionMarshaller<>(ConcurrentLinkedQueue::new));
		context.registerMarshaller(new SortedMapMarshaller<>(ConcurrentSkipListMap::new));
		context.registerMarshaller(new SortedSetMarshaller<>(ConcurrentSkipListSet::new));
		context.registerMarshaller(copyOnWriteMarshaller(collectionMarshaller, CopyOnWriteArrayList::new));
		context.registerMarshaller(copyOnWriteMarshaller(collectionMarshaller, CopyOnWriteArraySet::new));
		context.registerMarshaller(ProtoStreamMarshaller.of(TimeUnit.class));
	}

	@SuppressWarnings("unchecked")
	private static <T extends Collection<Object>> ProtoStreamMarshaller<T> copyOnWriteMarshaller(ProtoStreamMarshaller<Collection<Object>> sourceMarshaller, Function<Collection<Object>, T> factory) {
		// Use bulk operation for copy-on-write collections
		return sourceMarshaller.wrap((Class<T>) factory.apply(List.of()).getClass(), factory);
	}
}
