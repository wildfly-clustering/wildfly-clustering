/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import org.wildfly.clustering.server.util.Supplied;
import org.wildfly.clustering.session.cache.Contextual;

/**
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public interface SessionMetaDataEntry<C> extends Contextual<C> {

	SessionCreationMetaDataEntry<C> getCreationMetaDataEntry();

	SessionAccessMetaDataEntry getAccessMetaDataEntry();

	@Override
	default Supplied<C> getContext() {
		return this.getCreationMetaDataEntry().getContext();
	}
}
