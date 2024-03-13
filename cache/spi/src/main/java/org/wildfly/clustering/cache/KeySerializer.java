/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import java.util.function.Function;

import org.wildfly.clustering.marshalling.MappedSerializer;
import org.wildfly.clustering.marshalling.Serializer;

/**
 * Serializer for a key that delegates to the serializer of its identifier.
 * @param <I> the identifier type of the cache key
 * @param <K> the cache key type
 * @author Paul Ferraro
 */
public class KeySerializer<I, K extends Key<I>> extends MappedSerializer<K, I> {

	public KeySerializer(Serializer<I> serializer, Function<I, K> factory) {
		super(serializer, Key::getId, factory);
	}
}
