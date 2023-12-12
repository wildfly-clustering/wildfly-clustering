/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.user;

import org.wildfly.clustering.cache.infinispan.CacheKey;

/**
 * Cache key for the authentication cache entry.
 * @author Paul Ferraro
 */
public class UserContextKey extends CacheKey<String> {

	public UserContextKey(String id) {
		super(id);
	}
}
