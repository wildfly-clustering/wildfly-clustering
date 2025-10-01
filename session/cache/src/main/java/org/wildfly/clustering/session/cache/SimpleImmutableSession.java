/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache;

import java.util.Map;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.SimpleImmutableSessionMetaData;

/**
 * An immutable "snapshot" of a session which can be accessed outside the scope of a transaction.
 * @author Paul Ferraro
 */
public class SimpleImmutableSession extends AbstractImmutableSession {

	private final boolean valid;
	private final ImmutableSessionMetaData metaData;
	private final Map<String, Object> attributes;

	/**
	 * Creates an immutable session snapshot.
	 * @param session an immutable session
	 */
	public SimpleImmutableSession(ImmutableSession session) {
		super(session.getId());
		this.valid = session.isValid();
		this.metaData = new SimpleImmutableSessionMetaData(session.getMetaData());
		this.attributes = Map.copyOf(session.getAttributes());
	}

	@Override
	public boolean isValid() {
		return this.valid;
	}

	@Override
	public ImmutableSessionMetaData getMetaData() {
		return this.metaData;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}
}
