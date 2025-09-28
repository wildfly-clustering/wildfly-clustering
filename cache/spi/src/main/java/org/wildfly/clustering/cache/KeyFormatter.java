/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import java.util.function.Function;

import org.wildfly.clustering.marshalling.Formatter;

/**
 * A formatter of a cache key.
 * @author Paul Ferraro
 * @param <I> the key identifier type
 * @param <K> the key type
 */
public class KeyFormatter<I, K extends Key<I>> extends Formatter.Provided<K> {

	/**
	 * Creates a cache key formatter for the specified key class.
	 * @param keyClass a cache key class
	 * @param formatter the formatter for the cache key identifier
	 * @param factory a cache key factory
	 */
	public KeyFormatter(Class<K> keyClass, Formatter<I> formatter, Function<I, K> factory) {
		super(formatter.wrap(keyClass, Key::getId, factory));
	}
}
