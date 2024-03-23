/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.function.Function;

import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.KeyFormatter;
import org.wildfly.clustering.marshalling.Formatter;

/**
 * Base {@link org.wildfly.clustering.marshalling.Formatter} for cache keys containing session identifiers.
 * @param <K> the cache key type
 * @author Paul Ferraro
 */
public class SessionKeyFormatter<K extends Key<String>> extends KeyFormatter<String, K> {

	@SuppressWarnings("unchecked")
	protected SessionKeyFormatter(Function<String, K> factory) {
		this((Class<K>) factory.apply("").getClass(), factory);
	}

	private SessionKeyFormatter(Class<K> keyClass, Function<String, K> factory) {
		super(keyClass, Formatter.IDENTITY, factory);
	}
}
