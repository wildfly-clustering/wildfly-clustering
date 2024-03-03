/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.remote;

import java.util.function.Consumer;

import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.session.ImmutableSession;

/**
 * Configuration for an {@link HotRodSessionManager}.
 * @author Paul Ferraro
 */
public interface HotRodSessionManagerConfiguration extends RemoteCacheConfiguration {
	Registrar<Consumer<ImmutableSession>> getExpirationListenerRegistrar();
}
