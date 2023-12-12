/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.function.Function;

import org.wildfly.clustering.cache.infinispan.CacheKey;
import org.wildfly.clustering.marshalling.SimpleFormatter;

/**
 * Base {@link org.wildfly.clustering.marshalling.Formatter} for cache keys containing session identifiers.
 * @author Paul Ferraro
 */
public class SessionKeyFormatter<K extends CacheKey<String>> extends SimpleFormatter<K> {

	protected SessionKeyFormatter(Class<K> keyClass, Function<String, K> resolver) {
		super(keyClass, resolver, CacheKey::getId);
	}
}
