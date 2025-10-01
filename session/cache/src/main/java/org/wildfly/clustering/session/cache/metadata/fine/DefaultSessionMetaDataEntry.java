/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

/**
 * A session metadata entry.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class DefaultSessionMetaDataEntry<C> implements SessionMetaDataEntry<C> {

	private final SessionCreationMetaDataEntry<C> creationMetaDataEntry;
	private final SessionAccessMetaDataEntry accessMetaDataEntry;

	/**
	 * Creates a session metadata entry.
	 * @param creationMetaDataEntry the session creation metadata entry
	 * @param accessMetaDataEntry the session access metadata entry
	 */
	public DefaultSessionMetaDataEntry(SessionCreationMetaDataEntry<C> creationMetaDataEntry, SessionAccessMetaDataEntry accessMetaDataEntry) {
		this.creationMetaDataEntry = creationMetaDataEntry;
		this.accessMetaDataEntry = accessMetaDataEntry;
	}

	@Override
	public SessionCreationMetaDataEntry<C> getCreationMetaDataEntry() {
		return this.creationMetaDataEntry;
	}

	@Override
	public SessionAccessMetaDataEntry getAccessMetaDataEntry() {
		return this.accessMetaDataEntry;
	}
}
