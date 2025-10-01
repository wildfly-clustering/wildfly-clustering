/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.user;

import org.wildfly.clustering.cache.infinispan.CacheKey;

/**
 * The cache entry containing the associated sessions for a user.
 * @author Paul Ferraro
 */
public class UserSessionsKey extends CacheKey<String> {
	/**
	 * Creates a user session cache key.
	 * @param id the identifier of the user
	 */
	public UserSessionsKey(String id) {
		super(id);
	}
}
