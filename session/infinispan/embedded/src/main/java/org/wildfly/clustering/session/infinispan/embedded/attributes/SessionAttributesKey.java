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

	public SessionAttributesKey(String id) {
		super(id);
	}
}
