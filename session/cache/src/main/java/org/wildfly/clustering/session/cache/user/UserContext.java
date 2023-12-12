/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import org.wildfly.clustering.server.util.Supplied;

/**
 * @author Paul Ferraro
 */
public interface UserContext<C, T> {

	C getPersistentContext();

	Supplied<T> getTransientContext();
}
