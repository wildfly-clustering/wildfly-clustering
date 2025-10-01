/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.metadata;

import org.wildfly.clustering.cache.infinispan.CacheKey;

/**
 * Cache key for the session creation meta data entry.
 * @author Paul Ferraro
 */
public class SessionCreationMetaDataKey extends CacheKey<String> {
	/**
	 * Creates a session creation metadata key.
	 * @param id the identifier of the session
	 */
	public SessionCreationMetaDataKey(String id) {
		super(id);
	}
}
