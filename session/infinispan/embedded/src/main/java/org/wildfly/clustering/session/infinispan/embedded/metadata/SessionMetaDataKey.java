/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.metadata;

import org.wildfly.clustering.cache.infinispan.CacheKey;

/**
 * Cache key for the session meta data entry.
 * @author Paul Ferraro
 */
public class SessionMetaDataKey extends CacheKey<String> {

	public SessionMetaDataKey(String id) {
		super(id);
	}
}
