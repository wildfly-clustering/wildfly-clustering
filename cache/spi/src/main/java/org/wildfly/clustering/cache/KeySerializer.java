/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import java.util.function.Function;

import org.wildfly.clustering.marshalling.Serializer;

/**
 * A serializer for a key.
 * @author Paul Ferraro
 * @param <I> the key identifier type
 * @param <K> the key type
 */
public class KeySerializer<I, K extends Key<I>> extends Serializer.Provided<K> {

	public KeySerializer(Serializer<I> serializer, Function<I, K> factory) {
		super(serializer.wrap(Key::getId, factory));
	}
}
