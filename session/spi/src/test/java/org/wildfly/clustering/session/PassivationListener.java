/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

import java.util.Map;

/**
 * @author Paul Ferraro
 */
public interface PassivationListener<DC> {

	void passivated(Map.Entry<ImmutableSession, DC> entry);

	void activated(Map.Entry<ImmutableSession, DC> entry);
}
