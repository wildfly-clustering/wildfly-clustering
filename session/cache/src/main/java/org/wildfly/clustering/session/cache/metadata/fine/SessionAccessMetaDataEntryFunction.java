/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;

import org.wildfly.clustering.cache.function.RemappingFunction;
import org.wildfly.clustering.server.offset.Offset;

/**
 * A remapping function the session access metadata entry.
 * @author Paul Ferraro
 */
public class SessionAccessMetaDataEntryFunction extends RemappingFunction<SessionAccessMetaDataEntry, SessionAccessMetaDataEntryOffsets> {

	/**
	 * Creates a session access metadata entry function.
	 * @param values the session access metadata offset values.
	 */
	public SessionAccessMetaDataEntryFunction(MutableSessionAccessMetaDataOffsetValues values) {
		this(new SessionAccessMetaDataEntryOffsets() {
			@Override
			public Offset<Duration> getSinceCreationOffset() {
				return values.getSinceCreation().getOffset();
			}

			@Override
			public Offset<Duration> getLastAccessOffset() {
				return values.getLastAccess().getOffset();
			}
		});
	}

	/**
	 * Creates a a session access metadata entry function.
	 * @param offsets the set of session access metadata offsets
	 */
	public SessionAccessMetaDataEntryFunction(SessionAccessMetaDataEntryOffsets offsets) {
		super(offsets);
	}
}
