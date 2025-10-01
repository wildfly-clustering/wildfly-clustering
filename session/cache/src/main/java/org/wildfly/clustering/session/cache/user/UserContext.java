/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import org.wildfly.clustering.server.util.Supplied;

/**
 * Cache entry storing persistent and transient user context.
 * @param <PC> the persistent context type
 * @param <TC> the transient context type
 * @author Paul Ferraro
 */
public interface UserContext<PC, TC> {
	/**
	 * Returns the persistent context of the associated user.
	 * @return the persistent context of the associated user.
	 */
	PC getPersistentContext();

	/**
	 * Returns the transient context of the associated user.
	 * @return the transient context of the associated user.
	 */
	Supplied<TC> getTransientContext();
}
