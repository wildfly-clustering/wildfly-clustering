/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Paul Ferraro
 */
public abstract class AbstractSessionAttributes implements SessionAttributes {

	private final Map<String, Object> attributes;

	protected AbstractSessionAttributes(Map<String, Object> attributes) {
		this.attributes = Collections.unmodifiableMap(attributes);
	}

	@Override
	public Set<String> keySet() {
		return this.attributes.keySet();
	}

	@Override
	public Collection<Object> values() {
		return this.attributes.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return this.attributes.entrySet();
	}
}
