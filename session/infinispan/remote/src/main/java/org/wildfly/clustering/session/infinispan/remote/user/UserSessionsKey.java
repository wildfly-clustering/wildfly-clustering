/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.user;

import org.wildfly.clustering.cache.infinispan.CacheKey;

/**
 * The cache key for user sessions entries.
 * @author Paul Ferraro
 */
public class UserSessionsKey extends CacheKey<String> {
	/**
	 * Creates a user sessions key.
	 * @param id the identifier of the user
	 */
	public UserSessionsKey(String id) {
		super(id);
	}
}
