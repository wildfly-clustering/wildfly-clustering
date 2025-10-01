/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;


import org.infinispan.util.function.SerializablePredicate;
import org.wildfly.clustering.session.infinispan.embedded.metadata.SessionMetaDataKey;

/**
 * Cache key filters for use by cache streams.
 * @author Paul Ferraro
 */
public enum SessionCacheKeyFilter implements SerializablePredicate<Object> {
	/** The filter selecting session metadata keys */
	META_DATA(SessionMetaDataKey.class);

	private final Class<?> keyClass;

	SessionCacheKeyFilter(Class<?> keyClass) {
		this.keyClass = keyClass;
	}

	@Override
	public boolean test(Object key) {
		return this.keyClass.isInstance(key);
	}
}
