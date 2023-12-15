/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import org.wildfly.clustering.server.util.Supplied;

/**
 * Implemented by object with a supplied context.
 * @author Paul Ferraro
 */
public interface Contextual<C> {

	/**
	 * Returns the context as a supplied value.
	 * @return a supplied context.
	 */
	Supplied<C> getContext();
}
