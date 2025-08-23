/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import org.wildfly.clustering.session.ImmutableSession;

/**
 * Abstract session implementation containing {@link #equals(Object)}, {@link #hashCode()}, and {@link #toString()} methods.
 * @author Paul Ferraro
 */
public abstract class AbstractImmutableSession implements ImmutableSession {

	private final String id;

	public AbstractImmutableSession(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ImmutableSession session)) return false;
		return this.id.equals(session.getId());
	}

	@Override
	public String toString() {
		return this.id;
	}
}
