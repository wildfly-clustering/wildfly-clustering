/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import org.wildfly.clustering.server.context.Contextual;
import org.wildfly.clustering.session.Session;

/**
 * A completable session.
 * @author Paul Ferraro
 */
public interface ContextualSession<C> extends Contextual, Session<C> {
}
