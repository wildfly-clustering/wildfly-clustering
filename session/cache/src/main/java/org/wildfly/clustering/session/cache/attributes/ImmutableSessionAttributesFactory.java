/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import java.util.Map;

import org.wildfly.clustering.cache.CacheEntryLocator;

/**
 * Factory for creating a map of session attributes.
 * @author Paul Ferraro
 * @param <V> attributes cache entry type
 */
public interface ImmutableSessionAttributesFactory<V> extends CacheEntryLocator<String, V> {
	/**
	 * Creates an immutable session attributes.
	 * @param id the identifier of a session
	 * @param value the session attributes cache entry
	 * @return a map of session attributes
	 */
	Map<String, Object> createImmutableSessionAttributes(String id, V value);
}
