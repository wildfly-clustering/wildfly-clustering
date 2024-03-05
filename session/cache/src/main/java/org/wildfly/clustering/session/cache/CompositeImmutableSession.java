/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache;

import java.util.Map;

import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * Generic immutable session implementation - independent of cache mapping strategy.
 * @author Paul Ferraro
 */
public class CompositeImmutableSession extends AbstractImmutableSession {

	private final ImmutableSessionMetaData metaData;
	private final Map<String, Object> attributes;

	public CompositeImmutableSession(String id, ImmutableSessionMetaData metaData, Map<String, Object> attributes) {
		super(id);
		this.metaData = metaData;
		this.attributes = attributes;
	}

	@Override
	public boolean isValid() {
		return !this.metaData.isExpired();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	@Override
	public ImmutableSessionMetaData getMetaData() {
		return this.metaData;
	}
}
