/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.function.Supplier;

import org.wildfly.clustering.session.Session;

/**
 * A session whose lifecycle is managed by a {@link org.wildfly.clustering.server.cache.Cache}.
 * @param <C> the context type
 * @author Paul Ferraro
 */
public interface CacheableSession<C> extends Session<C>, Supplier<Session<C>> {
}
