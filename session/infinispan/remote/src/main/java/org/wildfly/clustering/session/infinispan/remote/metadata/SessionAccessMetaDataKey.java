/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.metadata;

import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheKey;

/**
 * Cache key for the session access meta data entry.
 * @author Paul Ferraro
 */
public class SessionAccessMetaDataKey extends RemoteCacheKey<String> {

	public SessionAccessMetaDataKey(String id) {
		super(id);
	}
}
