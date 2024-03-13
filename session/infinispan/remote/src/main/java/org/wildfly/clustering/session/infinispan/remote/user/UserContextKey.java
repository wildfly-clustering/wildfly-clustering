/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.user;

import org.wildfly.clustering.cache.infinispan.CacheKey;

/**
 * @author Paul Ferraro
 */
public class UserContextKey extends CacheKey<String> {

	public UserContextKey(String id) {
		super(id);
	}
}
