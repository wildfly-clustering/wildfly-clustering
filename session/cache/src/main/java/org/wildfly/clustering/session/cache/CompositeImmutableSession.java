/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionAttributes;
import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * Generic immutable session implementation - independent of cache mapping strategy.
 * @author Paul Ferraro
 */
public class CompositeImmutableSession implements ImmutableSession {

	private final String id;
	private final ImmutableSessionMetaData metaData;
	private final ImmutableSessionAttributes attributes;

	public CompositeImmutableSession(String id, ImmutableSessionMetaData metaData, ImmutableSessionAttributes attributes) {
		this.id = id;
		this.metaData = metaData;
		this.attributes = attributes;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public boolean isValid() {
		return !this.metaData.isExpired();
	}

	@Override
	public ImmutableSessionAttributes getAttributes() {
		return this.attributes;
	}

	@Override
	public ImmutableSessionMetaData getMetaData() {
		return this.metaData;
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ImmutableSession)) return false;
		ImmutableSession session = (ImmutableSession) object;
		return this.id.equals(session.getId());
	}

	@Override
	public String toString() {
		return this.id.toString();
	}
}
