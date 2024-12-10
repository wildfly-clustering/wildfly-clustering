/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;


import java.util.Map;
import java.util.function.Predicate;

import org.infinispan.util.function.SerializablePredicate;
import org.wildfly.clustering.session.infinispan.embedded.metadata.SessionMetaDataKey;

/**
 * Cache entry filters for use by cache streams.
 * @author Paul Ferraro
 */
public enum SessionCacheEntryFilter implements SerializablePredicate<Map.Entry<Object, Object>> {
	META_DATA(SessionMetaDataKey.class);

	private final Class<?> keyClass;

	SessionCacheEntryFilter(Class<?> keyClass) {
		this.keyClass = keyClass;
	}

	@Override
	public boolean test(Map.Entry<Object, Object> entry) {
		return this.keyClass.isInstance(entry.getKey());
	}

	@SuppressWarnings("unchecked")
	public <K, V> Predicate<Map.Entry<? super K, ? super V>> cast() {
		return (Predicate<Map.Entry<? super K, ? super V>>) (Predicate<?>) this;
	}
}
