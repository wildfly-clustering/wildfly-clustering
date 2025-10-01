/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded.attributes;

import org.wildfly.clustering.cache.infinispan.CacheKey;

/**
 * Cache key for session attributes.
 * @author Paul Ferraro
 */
public class SessionAttributesKey extends CacheKey<String> {
	/**
	 * Creates a session attribute key.
	 * @param id the identifier of this session
	 */
	public SessionAttributesKey(String id) {
		super(id);
	}
}
