/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;

import org.wildfly.clustering.session.ImmutableSession;

/**
 * @author Paul Ferraro
 */
public interface PassivationListener<C> {

	void passivated(Map.Entry<ImmutableSession, C> entry);

	void activated(Map.Entry<ImmutableSession, C> entry);
}
