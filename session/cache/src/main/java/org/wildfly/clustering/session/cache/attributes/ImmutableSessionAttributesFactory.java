/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import java.util.Map;

import org.wildfly.clustering.cache.Locator;

/**
 * Factory for creating a map of session attributes.
 * @author Paul Ferraro
 * @param <V> attributes cache entry type
 */
public interface ImmutableSessionAttributesFactory<V> extends Locator<String, V> {
	Map<String, Object> createImmutableSessionAttributes(String id, V value);
}
