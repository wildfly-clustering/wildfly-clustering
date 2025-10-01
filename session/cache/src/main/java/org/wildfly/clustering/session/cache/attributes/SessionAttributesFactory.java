/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import org.wildfly.clustering.cache.CacheEntryCreator;
import org.wildfly.clustering.cache.CacheEntryRemover;
import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * Factory for creating a {@link SessionAttributes} object.
 * @param <C> the deployment context type
 * @param <V> the marshalled value type
 * @author Paul Ferraro
 */
public interface SessionAttributesFactory<C, V> extends ImmutableSessionAttributesFactory<V>, CacheEntryCreator<String, V, Void>, CacheEntryRemover<String>, AutoCloseable {
	/**
	 * Create a {@link SessionAttributes} object.
	 * @param id the identifier of a session
	 * @param value the marshalled value type
	 * @param metaData the metadata of a session
	 * @param context the context of a session
	 * @return a {@link SessionAttributes} object.
	 */
	SessionAttributes createSessionAttributes(String id, V value, ImmutableSessionMetaData metaData, C context);

	@Override
	void close();
}
