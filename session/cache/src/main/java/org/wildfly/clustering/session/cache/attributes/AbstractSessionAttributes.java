/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.util.Reference;

/**
 * A session attributes implementation based on a map.
 * @author Paul Ferraro
 */
public abstract class AbstractSessionAttributes implements SessionAttributes {
	private static final Function<Set<String>, Set<String>> COPY_KEYS = Set::copyOf;
	private static final Function<Set<Map.Entry<String, Object>>, Set<Map.Entry<String, Object>>> COPY_ENTRIES = Set::copyOf;
	private static final Function<Collection<Object>, Collection<Object>> COPY_VALUES = List::copyOf;
	private static final Function<Map<String, Object>, Set<String>> KEYS = Function.of(Map::keySet, COPY_KEYS);
	private static final Function<Map<String, Object>, Set<Map.Entry<String, Object>>> ENTRIES = Function.of(Map::entrySet, COPY_ENTRIES);
	private static final Function<Map<String, Object>, Collection<Object>> VALUES = Function.of(Map::values, COPY_VALUES);

	private final Reference<Map<String, Object>> attributes;

	/**
	 * Creates the attributes of a session.
	 * @param attributes a map of attributes.
	 */
	protected AbstractSessionAttributes(Reference<Map<String, Object>> attributes) {
		this.attributes = attributes;
	}

	@Override
	public Set<String> keySet() {
		return this.attributes.getReader().map(KEYS).get();
	}

	@Override
	public Collection<Object> values() {
		return this.attributes.getReader().map(VALUES).get();
	}

	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		return this.attributes.getReader().map(ENTRIES).get();
	}
}
