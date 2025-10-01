/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.remote.attributes;

import org.wildfly.clustering.cache.infinispan.CacheKey;

/**
 * Cache key for session attributes.
 * @author Paul Ferraro
 */
public class SessionAttributesKey extends CacheKey<String> {
	/**
	 * Creates a session attributes key.
	 * @param id the identifier of the session
	 */
	public SessionAttributesKey(String id) {
		super(id);
	}
}
