/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.user;

import org.wildfly.clustering.cache.infinispan.CacheKey;

/**
 * The cache key for user context entries.
 * @author Paul Ferraro
 */
public class UserContextKey extends CacheKey<String> {
	/**
	 * Creates a user context key.
	 * @param id the identifier of the user
	 */
	public UserContextKey(String id) {
		super(id);
	}
}
