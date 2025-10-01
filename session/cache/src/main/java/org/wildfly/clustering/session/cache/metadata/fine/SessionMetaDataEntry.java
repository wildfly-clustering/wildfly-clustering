/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import org.wildfly.clustering.server.util.Supplied;
import org.wildfly.clustering.session.cache.Contextual;

/**
 * Encapsulates fine-granularity mapping of session metadata using separate entries for creation vs access metadata.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public interface SessionMetaDataEntry<C> extends Contextual<C> {
	/**
	 * Returns the creation metadata entry.
	 * @return the creation metadata entry.
	 */
	SessionCreationMetaDataEntry<C> getCreationMetaDataEntry();

	/**
	 * Returns the access metadata entry.
	 * @return the access metadata entry.
	 */
	SessionAccessMetaDataEntry getAccessMetaDataEntry();

	@Override
	default Supplied<C> getContext() {
		return this.getCreationMetaDataEntry().getContext();
	}
}
